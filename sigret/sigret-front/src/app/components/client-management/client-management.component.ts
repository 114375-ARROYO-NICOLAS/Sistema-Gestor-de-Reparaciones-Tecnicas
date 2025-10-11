import { Component, OnInit, signal, computed, inject, ChangeDetectionStrategy, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { ToolbarModule } from 'primeng/toolbar';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { BadgeModule } from 'primeng/badge';

import { ClientService } from '../../services/client.service';
import {
  ClientCreateRequest,
  ClientUpdateRequest,
  ClientListResponse,
  ClientListDto,
  ClientResponse,
  PersonType,
  DocumentType,
  Address,
  GooglePlacesData
} from '../../models/client.model';
import { environment } from '../../../environments/environment';

// Google Maps type declarations
declare const google: any;

@Component({
  selector: 'app-client-management',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    ButtonModule,
    TableModule,
    DialogModule,
    InputTextModule,
    SelectModule,
    TagModule,
    ToolbarModule,
    ConfirmDialogModule,
    ToastModule,
    ProgressSpinnerModule,
    CardModule,
    DividerModule,
    BadgeModule
  ],
  templateUrl: './client-management.component.html',
  styleUrls: ['./client-management.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ClientManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly router = inject(Router);
  public readonly clientService = inject(ClientService);

  // Signals
  public readonly clients = signal<ClientListDto[]>([]);
  public readonly personTypes = signal<PersonType[]>([]);
  public readonly documentTypes = signal<DocumentType[]>([]);
  public readonly isLoading = signal(false);
  public readonly isSaving = signal(false);
  public readonly showClientDialog = signal(false);
  public readonly isEditMode = signal(false);
  public readonly selectedClient = signal<ClientListDto | null>(null);
  public readonly totalRecords = signal(0);
  public readonly currentPage = signal(0);
  public readonly pageSize = 10;
  public readonly formValid = signal(false);

  // Address management signals
  public readonly addresses = signal<Address[]>([]);
  public readonly currentAddress = signal<Address>({});
  public readonly isAddingAddress = signal(false);
  private readonly initialAddresses = signal<Address[]>([]);

  // Flag to prevent infinite loop in lazy load
  private isLoadingData = false;

  // Filters
  public filterBusqueda = signal<string>('');

  // Computed signals
  public readonly totalClients = computed(() => this.totalRecords());
  public readonly activeClients = computed(() => this.clients().filter(c => c.activo ?? true).length);

  // Check if addresses have changed
  public readonly addressesChanged = computed(() => {
    const current = this.addresses();
    const initial = this.initialAddresses();
    return JSON.stringify(current) !== JSON.stringify(initial);
  });

  // Can save if form is valid OR addresses changed (for edit mode)
  public readonly canSaveClient = computed(() => {
    if (!this.isEditMode()) {
      // Create mode: form must be valid
      return this.formValid();
    } else {
      // Edit mode: Allow save if addresses changed OR form is valid and dirty
      return this.addressesChanged() || (this.formValid() && this.clientForm.dirty);
    }
  });

  // Form
  public clientForm: FormGroup;

  // Sex options
  public readonly sexOptions = [
    { label: 'Masculino', value: 'M' },
    { label: 'Femenino', value: 'F' }
  ];

  // Google Places - Classic Autocomplete API
  @ViewChild('addressInput') addressInput?: ElementRef<HTMLInputElement>;
  private placeAutocomplete: any = null;
  private googleMapsApiKey: string = environment.googleMapsApiKey;
  private googleMapsLoaded = false;
  private scrollListener: (() => void) | null = null;

  constructor() {
    this.clientForm = this.createClientForm();
    
    // Subscribe to form status changes to update signal
    this.clientForm.statusChanges.subscribe(() => {
      this.formValid.set(this.clientForm.valid);
    });
    
    // Initialize form valid state
    this.formValid.set(this.clientForm.valid);
  }

  ngOnInit(): void {
    // DO NOT call loadClients() here - p-table with [lazy]="true" will trigger it automatically
    this.loadPersonTypes();
    this.loadDocumentTypes();
    this.loadGoogleMaps();
  }

  private async loadGoogleMaps(): Promise<void> {
    // Check if already loaded
    if (this.googleMapsLoaded || typeof google !== 'undefined') {
      this.googleMapsLoaded = true;
      return;
    }

    return new Promise((resolve, reject) => {
      try {
        // Create script element for classic Google Maps Places API
        const script = document.createElement('script');
        script.src = `https://maps.googleapis.com/maps/api/js?key=${this.googleMapsApiKey}&libraries=places&loading=async&callback=initGoogleMaps`;
        script.async = true;
        script.defer = true;

        // Global callback function
        (window as any).initGoogleMaps = () => {
          this.googleMapsLoaded = true;
          resolve();
        };

        // Error handling
        script.onerror = (error) => {
          console.error('Error loading Google Maps API:', error);
          this.messageService.add({
            severity: 'warn',
            summary: 'Advertencia',
            detail: 'No se pudo cargar Google Maps API. Las direcciones pueden no funcionar correctamente.',
            life: 5000
          });
          reject(error);
        };

        // Append script to document
        document.head.appendChild(script);
      } catch (error) {
        console.error('Error loading Google Maps API:', error);
        this.messageService.add({
          severity: 'warn',
          summary: 'Advertencia',
          detail: 'No se pudo cargar Google Maps API.',
          life: 5000
        });
        reject(error);
      }
    });
  }

  private createClientForm(): FormGroup {
    return this.fb.group({
      tipoPersonaId: [1, Validators.required], // 1 = Persona Física por defecto
      tipoDocumentoId: [1, Validators.required], // 1 = DNI por defecto
      documento: ['', [Validators.required, Validators.minLength(6)]],
      nombre: ['', Validators.required], // Requerido por defecto (Persona Física)
      apellido: ['', Validators.required], // Requerido por defecto (Persona Física)
      razonSocial: [''],
      sexo: ['']
    });
  }

  loadClients(event?: any): void {
    // PREVENT INFINITE LOOP: If already loading, do nothing
    if (this.isLoadingData) {
      return;
    }

    this.isLoadingData = true;
    this.isLoading.set(true);

    const page = event ? event.first / event.rows : this.currentPage();
    this.currentPage.set(page);

    const filters = {
      page,
      size: this.pageSize,
      filtro: this.filterBusqueda() || undefined,
      sort: 'id,DESC'
    };

    this.clientService.getClients(filters).subscribe({
      next: (response: ClientListResponse) => {
        // Process client data
        
        this.clients.set(response.content);
        this.totalRecords.set(response.totalElements);
        this.isLoading.set(false);
        this.isLoadingData = false;
      },
      error: (error) => {
        console.error('Error al cargar clientes:', error);

        let errorMessage = 'Error al cargar clientes';

        if (error.status === 403) {
          errorMessage = 'No tienes permisos para acceder a la gestión de clientes. Contacta al administrador.';
        } else if (error.status === 401) {
          errorMessage = 'Tu sesión ha expirado. Por favor, inicia sesión nuevamente.';
        } else {
          errorMessage = error.message || 'Error desconocido';
        }

        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: errorMessage,
          life: 5000
        });
        this.isLoading.set(false);
        this.isLoadingData = false;
      }
    });
  }

  onFilterChange(): void {
    this.currentPage.set(0);
    // Trigger lazy load manually when filters change
    this.loadClients({ first: 0, rows: this.pageSize });
  }

  loadPersonTypes(): void {
    this.clientService.getPersonTypes().subscribe({
      next: (types) => {
        this.personTypes.set(types);
      },
      error: (error) => {
        console.error('Error loading person types:', error);
        // Don't show error to user - these are optional catalog data
        this.personTypes.set([]);
      }
    });
  }

  loadDocumentTypes(): void {
    this.clientService.getDocumentTypes().subscribe({
      next: (types) => {
        this.documentTypes.set(types);
      },
      error: (error) => {
        console.error('Error loading document types:', error);
        // Don't show error to user - these are optional catalog data
        this.documentTypes.set([]);
      }
    });
  }

  openCreateDialog(): void {
    this.isEditMode.set(false);
    this.selectedClient.set(null);
    this.addresses.set([]); // Reset addresses
    this.initialAddresses.set([]); // Save initial state
    
    // Reset form with default values
    this.clientForm.reset({
      tipoPersonaId: 1,
      tipoDocumentoId: 1,
      documento: '',
      nombre: '',
      apellido: '',
      razonSocial: '',
      sexo: ''
    });
    
    // Aplicar validaciones según el tipo de persona por defecto
    setTimeout(() => {
      this.onPersonTypeChange();
      this.clientForm.markAsUntouched();
      this.clientForm.markAsPristine();
    });
    
    this.showClientDialog.set(true);
  }

  openEditDialog(client: ClientListDto): void {
    this.isEditMode.set(true);
    this.selectedClient.set(client);

    // Load full client details to get addresses
    this.clientService.getClientById(client.id).subscribe({
      next: (clientDetails) => {
        const currentAddresses = clientDetails.direcciones || [];
        this.addresses.set(currentAddresses);
        this.initialAddresses.set(JSON.parse(JSON.stringify(currentAddresses))); // Deep copy for comparison

        this.clientForm.patchValue({
          tipoPersonaId: this.getPersonTypeId(clientDetails.tipoPersona),
          tipoDocumentoId: this.getDocumentTypeId(clientDetails.tipoDocumento),
          documento: clientDetails.documento,
          nombre: clientDetails.nombre,
          apellido: clientDetails.apellido,
          razonSocial: clientDetails.razonSocial,
          sexo: clientDetails.sexo
        });

        this.showClientDialog.set(true);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'Error al cargar los detalles del cliente'
        });
      }
    });
  }

  closeClientDialog(): void {
    this.showClientDialog.set(false);
    this.clientForm.reset();
    this.selectedClient.set(null);
    this.isEditMode.set(false);
    this.addresses.set([]);
    this.currentAddress.set({});
    this.isAddingAddress.set(false);

    // Clean up autocomplete and pac-container
    this.cleanupGooglePlaces();
  }

  // Clean up Google Places resources
  private cleanupGooglePlaces(): void {
    // Remove scroll listener
    this.removeScrollListener();

    // Clean up autocomplete (Classic API)
    if (this.placeAutocomplete) {
      try {
        google.maps.event.clearInstanceListeners(this.placeAutocomplete);
        this.placeAutocomplete = null;
      } catch (e) {
        console.warn('Error cleaning up autocomplete:', e);
      }
    }

    // Remove pac-container from DOM
    this.removePacContainer();
  }

  // Navigate to client detail page
  viewClientDetail(client: ClientListDto): void {
    this.router.navigate(['/clientes', client.id]);
  }

  // Google Places - Classic Autocomplete API
  private async initializeGooglePlaces(): Promise<void> {
    if (!this.addressInput?.nativeElement) {
      return;
    }

    try {
      // Ensure Google Maps is loaded
      if (!this.googleMapsLoaded) {
        await this.loadGoogleMaps();
      }

      // Wait for Google Maps libraries to be fully loaded
      await new Promise(resolve => setTimeout(resolve, 300));

      if (typeof google === 'undefined' || !google.maps || !google.maps.places) {
        console.error('Google Maps Places library not available');
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Google Maps no está disponible. Por favor, recarga la página.',
          life: 5000
        });
        return;
      }

      // Clean up existing autocomplete if any
      if (this.placeAutocomplete) {
        try {
          google.maps.event.clearInstanceListeners(this.placeAutocomplete);
          this.placeAutocomplete = null;
        } catch (e) {
          console.warn('Error removing previous autocomplete:', e);
        }
      }

      const inputElement = this.addressInput.nativeElement;

      // Create session token for cost optimization
      let sessionToken = new google.maps.places.AutocompleteSessionToken();

      // Create Autocomplete instance
      this.placeAutocomplete = new google.maps.places.Autocomplete(inputElement, {
        componentRestrictions: { country: 'ar' },
        fields: ['place_id', 'formatted_address', 'address_components', 'geometry', 'name'],
        sessionToken: sessionToken,
        types: ['geocode', 'establishment']
      });

      // Listen for place selection
      this.placeAutocomplete.addListener('place_changed', () => {
        const place = this.placeAutocomplete.getPlace();

        if (!place || !place.place_id) {
          return;
        }

        // Process the selected place
        this.processPlaceDetails(place);

        // Reset session token after successful selection
        sessionToken = new google.maps.places.AutocompleteSessionToken();
      });

      // Setup scroll listener to hide dropdown when scrolling
      this.setupScrollListener();
    } catch (error) {
      console.error('Error initializing Google Places:', error);
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo inicializar el buscador de direcciones.',
        life: 5000
      });
    }
  }

  // Setup scroll listener to hide pac-container when scrolling
  private setupScrollListener(): void {
    // Remove existing listener if any
    this.removeScrollListener();

    // Find the dialog content element that contains the scrollable area
    const dialogContent = document.querySelector('.p-dialog-content');

    if (dialogContent) {
      this.scrollListener = () => {
        this.hidePacContainer();
      };

      dialogContent.addEventListener('scroll', this.scrollListener);
    }
  }

  // Remove scroll listener
  private removeScrollListener(): void {
    if (this.scrollListener) {
      const dialogContent = document.querySelector('.p-dialog-content');
      if (dialogContent) {
        dialogContent.removeEventListener('scroll', this.scrollListener);
      }
      this.scrollListener = null;
    }
  }

  // Hide the pac-container dropdown
  private hidePacContainer(): void {
    const pacContainers = document.querySelectorAll('.pac-container');
    pacContainers.forEach(container => {
      (container as HTMLElement).style.display = 'none';
    });
  }

  // Remove pac-container from DOM completely
  private removePacContainer(): void {
    const pacContainers = document.querySelectorAll('.pac-container');
    pacContainers.forEach(container => {
      container.remove();
    });
  }

  // Process place details from the Classic Places API
  private processPlaceDetails(place: any): void {
    if (!place) {
      return;
    }

    try {
      // Process address components from classic API
      const addressComponents = place.address_components || [];

      const getComponent = (types: string[]) => {
        const component = addressComponents.find((c: any) =>
          types.some(type => c.types.includes(type))
        );
        return component;
      };

      const streetNumber = getComponent(['street_number'])?.long_name || '';
      const route = getComponent(['route'])?.long_name || '';
      const locality = getComponent(['locality', 'administrative_area_level_2'])?.long_name || '';
      const adminArea1 = getComponent(['administrative_area_level_1'])?.long_name || '';
      const country = getComponent(['country'])?.long_name || '';
      const postalCode = getComponent(['postal_code'])?.long_name || '';

      const addressData: Address = {
        placeId: place.place_id || undefined,
        calle: route || undefined,
        numero: streetNumber || undefined,
        ciudad: locality || undefined,
        provincia: adminArea1 || undefined,
        pais: country || undefined,
        codigoPostal: postalCode || undefined,
        direccionFormateada: place.formatted_address || undefined,
        latitud: place.geometry?.location?.lat() || undefined,
        longitud: place.geometry?.location?.lng() || undefined,
        esPrincipal: this.addresses().length === 0,
        googlePlacesData: {
          placeId: place.place_id || '',
          formattedAddress: place.formatted_address || '',
          geometry: {
            location: {
              lat: place.geometry?.location?.lat() || 0,
              lng: place.geometry?.location?.lng() || 0
            }
          },
          addressComponents: addressComponents.map((c: any) => ({
            longName: c.long_name || '',
            shortName: c.short_name || '',
            types: c.types || []
          }))
        }
      };

      this.currentAddress.set(addressData);
    } catch (error) {
      console.error('Error processing place:', error);
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Error al procesar la dirección seleccionada.',
        life: 5000
      });
    }
  }

  // Address management methods
  toggleAddingAddress(): void {
    this.isAddingAddress.set(!this.isAddingAddress());
    if (this.isAddingAddress()) {
      this.currentAddress.set({ esPrincipal: this.addresses().length === 0 });
      // Wait for DOM to render the container before initializing autocomplete
      setTimeout(() => this.initializeGooglePlaces(), 500);
    } else {
      this.currentAddress.set({});

      // Clean up autocomplete and pac-container
      this.cleanupGooglePlaces();

      if (this.addressInput?.nativeElement) {
        this.addressInput.nativeElement.value = '';
      }
    }
  }

  addAddress(): void {
    const address = this.currentAddress();

    // Validate that it has placeId or at least formatted address
    if (!address.placeId && !address.direccionFormateada) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Por favor, selecciona una dirección del autocompletado'
      });
      return;
    }

    // If this address is set as principal, unset others
    if (address.esPrincipal) {
      const updatedAddresses = this.addresses().map(addr => ({ ...addr, esPrincipal: false }));
      this.addresses.set(updatedAddresses);
    }

    this.addresses.update(addrs => [...addrs, address]);

    this.currentAddress.set({});
    this.isAddingAddress.set(false);

    // Clean up autocomplete and pac-container
    this.cleanupGooglePlaces();

    if (this.addressInput?.nativeElement) {
      this.addressInput.nativeElement.value = '';
    }

    this.messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Dirección agregada correctamente'
    });
  }

  removeAddress(index: number): void {
    const currentAddresses = this.addresses();
    const removedAddress = currentAddresses[index];
    const newAddresses = currentAddresses.filter((_, i) => i !== index);

    // If removed address was principal and there are other addresses, make the first one principal
    if (removedAddress.esPrincipal && newAddresses.length > 0) {
      newAddresses[0] = { ...newAddresses[0], esPrincipal: true };
    }

    this.addresses.set(newAddresses);

    this.messageService.add({
      severity: 'info',
      summary: 'Información',
      detail: 'Dirección eliminada'
    });
  }

  setPrimaryAddress(index: number): void {
    const updatedAddresses = this.addresses().map((addr, i) => ({
      ...addr,
      esPrincipal: i === index
    }));
    this.addresses.set(updatedAddresses);
  }

  getAddressDisplay(address: Address): string {
    if (address.direccionCompleta) return address.direccionCompleta;
    if (address.direccionFormateada) return address.direccionFormateada;
    if (address.googlePlacesData?.formattedAddress) return address.googlePlacesData.formattedAddress;

    const parts = [];
    if (address.calle) parts.push(address.calle);
    if (address.numero) parts.push(address.numero);
    if (address.ciudad) parts.push(address.ciudad);
    if (address.provincia) parts.push(address.provincia);
    if (address.pais) parts.push(address.pais);

    return parts.join(', ') || 'Dirección sin especificar';
  }

  // Open address in Google Maps
  openInGoogleMaps(address: Address): void {
    if (!address.latitud || !address.longitud) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Esta dirección no tiene coordenadas disponibles'
      });
      return;
    }

    // Build Google Maps URL with coordinates
    const mapsUrl = `https://www.google.com/maps/search/?api=1&query=${address.latitud},${address.longitud}`;

    // Open in new tab
    window.open(mapsUrl, '_blank');
  }

  onPersonTypeChange(): void {
    if (this.isNaturalPerson()) {
      this.clientForm.get('nombre')?.setValidators([Validators.required]);
      this.clientForm.get('apellido')?.setValidators([Validators.required]);
      this.clientForm.get('razonSocial')?.clearValidators();
    } else {
      this.clientForm.get('razonSocial')?.setValidators([Validators.required]);
      this.clientForm.get('nombre')?.clearValidators();
      this.clientForm.get('apellido')?.clearValidators();
    }
    this.clientForm.get('nombre')?.updateValueAndValidity();
    this.clientForm.get('apellido')?.updateValueAndValidity();
    this.clientForm.get('razonSocial')?.updateValueAndValidity();
  }

  isNaturalPerson(): boolean {
    const tipoPersonaId = this.clientForm.get('tipoPersonaId')?.value;
    return tipoPersonaId === 1; // 1 = Persona Física
  }

  saveClient(): void {
    // In edit mode, allow saving if only addresses changed (even if form is invalid)
    const canProceed = this.isEditMode()
      ? (this.clientForm.valid || this.addressesChanged())
      : this.clientForm.valid;

    if (canProceed) {
      this.isSaving.set(true);

      const formValue = this.clientForm.value;

      if (this.isEditMode()) {
        const updateData: ClientUpdateRequest = {
          nombre: formValue.nombre,
          apellido: formValue.apellido,
          razonSocial: formValue.razonSocial,
          tipoPersonaId: formValue.tipoPersonaId,
          tipoDocumentoId: formValue.tipoDocumentoId,
          documento: formValue.documento,
          sexo: formValue.sexo,
          direcciones: this.addresses().length > 0 ? this.addresses() : undefined
        };

        this.clientService.updateClient(this.selectedClient()!.id, updateData).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Cliente actualizado correctamente'
            });
            this.closeClientDialog();
            // Reload with current page
            this.loadClients({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
            this.isSaving.set(false);
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error.message || 'Error al actualizar cliente'
            });
            this.isSaving.set(false);
          }
        });
      } else {
        const createData: ClientCreateRequest = {
          nombre: formValue.nombre,
          apellido: formValue.apellido,
          razonSocial: formValue.razonSocial,
          tipoPersonaId: formValue.tipoPersonaId,
          tipoDocumentoId: formValue.tipoDocumentoId,
          documento: formValue.documento,
          sexo: formValue.sexo,
          direcciones: this.addresses().length > 0 ? this.addresses() : undefined
        };

        this.clientService.createClient(createData).subscribe({
          next: (response: ClientResponse) => {
            this.messageService.add({
              severity: 'success',
              summary: 'Cliente creado exitosamente',
              detail: `Se creó el cliente: ${response.nombreCompleto}`,
              life: 5000
            });
            this.closeClientDialog();
            // Reload from first page
            this.currentPage.set(0);
            this.loadClients({ first: 0, rows: this.pageSize });
            this.isSaving.set(false);
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error.message || 'Error al crear cliente'
            });
            this.isSaving.set(false);
          }
        });
      }
    }
  }

  reactivateClient(client: ClientListDto): void {
    this.confirmationService.confirm({
      message: `¿Desea reactivar al cliente "${client.nombreCompleto}"?`,
      header: 'Confirmar Reactivación',
      icon: 'pi pi-check-circle',
      acceptLabel: 'Sí, reactivar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.clientService.reactivateClient(client.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Cliente reactivado correctamente'
            });
            // Reload with current page
            this.loadClients({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error.message || 'Error al reactivar cliente'
            });
          }
        });
      }
    });
  }

  deactivateClient(client: ClientListDto): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea dar de baja al cliente "${client.nombreCompleto}"?`,
      header: 'Confirmar Baja',
      icon: 'pi pi-ban',
      acceptLabel: 'Sí, dar de baja',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.clientService.deactivateClient(client.id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Cliente dado de baja correctamente'
            });
            // Reload with current page
            this.loadClients({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error.message || 'Error al dar de baja al cliente'
            });
          }
        });
      }
    });
  }

  // Helper methods to get IDs from descriptions
  private getPersonTypeId(descripcion: string): number {
    const personType = this.personTypes().find(pt => pt.descripcion === descripcion);
    return personType?.id || 1;
  }

  private getDocumentTypeId(descripcion: string): number {
    const docType = this.documentTypes().find(dt => dt.descripcion === descripcion);
    return docType?.id || 1;
  }

  // Address helper methods for table display
  getPrimaryAddress(client: ClientListDto): Address | null {
    if (!client.direcciones || client.direcciones.length === 0) {
      return null;
    }
    
    // Find principal address first
    const principalAddress = client.direcciones.find(addr => addr.esPrincipal);
    if (principalAddress) {
      return principalAddress;
    }
    
    // If no principal address, return the first one
    return client.direcciones[0];
  }

  getPrimaryAddressDisplay(address: Address): string {
    if (address.direccionCompleta) return address.direccionCompleta;
    if (address.direccionFormateada) return address.direccionFormateada;
    if (address.googlePlacesData?.formattedAddress) return address.googlePlacesData.formattedAddress;

    const parts = [];
    if (address.calle) parts.push(address.calle);
    if (address.numero) parts.push(address.numero);
    if (address.ciudad) parts.push(address.ciudad);
    if (address.provincia) parts.push(address.provincia);

    return parts.join(', ') || 'Dirección sin especificar';
  }

  openAddressInMaps(address: string): void {
    if (!address || address.trim() === '') {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'No hay dirección disponible'
      });
      return;
    }

    // Build Google Maps URL with address text
    const encodedAddress = encodeURIComponent(address);
    const mapsUrl = `https://www.google.com/maps/search/?api=1&query=${encodedAddress}`;

    // Open in new tab
    window.open(mapsUrl, '_blank');
  }

  isMobile(): boolean {
    return window.innerWidth < 768;
  }
}

