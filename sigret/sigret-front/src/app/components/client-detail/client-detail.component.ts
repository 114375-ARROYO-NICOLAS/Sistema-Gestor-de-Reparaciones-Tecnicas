import { Component, OnInit, signal, inject, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { DividerModule } from 'primeng/divider';
import { BadgeModule } from 'primeng/badge';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { ClientService } from '../../services/client.service';
import { ClientResponse, ClientUpdateRequest, Address } from '../../models/client.model';
import { TipoContacto, ContactoCreateDto } from '../../models/contact.model';
import { EquipoService } from '../../services/equipo.service';
import { EquipoListDto } from '../../models/equipo.model';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CheckboxModule } from 'primeng/checkbox';
import { AutoComplete } from 'primeng/autocomplete';
import { environment } from '../../../environments/environment';

// Google Maps type declarations
declare const google: any;

@Component({
  selector: 'app-client-detail',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    ButtonModule,
    CardModule,
    TagModule,
    ProgressSpinnerModule,
    DividerModule,
    BadgeModule,
    DialogModule,
    InputTextModule,
    Select,
    ToastModule,
    ConfirmDialogModule,
    CheckboxModule,
    AutoComplete
  ],
  templateUrl: './client-detail.component.html',
  styleUrls: ['./client-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService, ConfirmationService]
})
export class ClientDetailComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);
  public readonly clientService = inject(ClientService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly equipoService = inject(EquipoService);
  private readonly sanitizer = inject(DomSanitizer);

  // Signals
  public readonly client = signal<ClientResponse | null>(null);
  public readonly equipos = signal<EquipoListDto[]>([]);
  public readonly isLoadingEquipos = signal(false);
  public readonly isLoading = signal(true);
  public readonly isSaving = signal(false);
  
  // Edit client
  public readonly showEditDialog = signal(false);
  public readonly personTypes = signal<any[]>([]);
  public readonly documentTypes = signal<any[]>([]);
  public clientEditForm: FormGroup;
  
  // Contact management
  public readonly showContactDialog = signal(false);
  public readonly tiposContacto = signal<TipoContacto[]>([]);
  public readonly isEditingContact = signal(false);
  public readonly editingContactIndex = signal<number>(-1);
  public contactForm: FormGroup;

  // Address management
  public readonly showAddressDialog = signal(false);
  public readonly isEditingAddress = signal(false);
  public readonly editingAddressIndex = signal<number>(-1);
  public readonly selectedAddressForMap = signal<number>(-1);
  public readonly currentAddressData = signal<Address>({});
  public readonly addressSuggestions = signal<any[]>([]);
  public selectedAddressSuggestion: string = '';

  // Google Places internals
  private autocompleteService: any = null;
  private placesService: any = null;
  private sessionToken: any = null;
  private readonly googleMapsApiKey = environment.googleMapsApiKey;
  private googleMapsLoaded = false;

  private clientId: number = 0;

  constructor() {
    this.contactForm = this.fb.group({
      tipoContactoId: [null, Validators.required],
      descripcion: ['', [Validators.required, Validators.maxLength(200)]]
    });
    
    this.clientEditForm = this.fb.group({
      tipoPersonaId: [1, Validators.required],
      tipoDocumentoId: [1, Validators.required],
      documento: ['', [Validators.required, Validators.minLength(6)]],
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      razonSocial: [''],
      sexo: ['']
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.clientId = +id;
      this.loadClient(this.clientId);
      this.loadEquipos(this.clientId);
      this.loadTiposContacto();
      this.loadPersonTypes();
      this.loadDocumentTypes();
      this.loadGoogleMaps();
    } else {
      this.goBack();
    }
  }

  private loadPersonTypes(): void {
    this.clientService.getPersonTypes().subscribe({
      next: (types) => {
        this.personTypes.set(types);
      },
      error: (error) => {
        console.error('Error loading person types:', error);
        this.personTypes.set([]);
      }
    });
  }

  private loadDocumentTypes(): void {
    this.clientService.getDocumentTypes().subscribe({
      next: (types) => {
        this.documentTypes.set(types);
      },
      error: (error) => {
        console.error('Error loading document types:', error);
        this.documentTypes.set([]);
      }
    });
  }

  private loadTiposContacto(): void {
    this.clientService.getTiposContacto().subscribe({
      next: (tipos) => {
        this.tiposContacto.set(tipos);
      },
      error: (error) => {
        console.error('Error loading tipos contacto:', error);
        // Don't show error to user - tipos contacto are optional catalog data
        // Set empty array to prevent issues
        this.tiposContacto.set([]);
      }
    });
  }

  private loadClient(id: number): void {
    this.isLoading.set(true);
    this.clientService.getClientById(id).subscribe({
      next: (client) => {
        this.client.set(client);
        this.isLoading.set(false);
        // Force change detection for OnPush strategy
        this.cdr.markForCheck();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'Error al cargar cliente'
        });
        this.isLoading.set(false);
        this.goBack();
      }
    });
  }

  private loadEquipos(clienteId: number): void {
    this.isLoadingEquipos.set(true);
    this.equipoService.getEquiposByCliente(clienteId).subscribe({
      next: (equipos) => {
        this.equipos.set(equipos);
        this.isLoadingEquipos.set(false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.equipos.set([]);
        this.isLoadingEquipos.set(false);
      }
    });
  }

  getMapUrl(): SafeResourceUrl | null {
    const client = this.client();
    if (!client?.direcciones?.length) return null;

    const direcciones = client.direcciones;
    const selectedIdx = this.selectedAddressForMap();

    let target: Address | undefined;
    if (selectedIdx >= 0 && selectedIdx < direcciones.length) {
      target = direcciones[selectedIdx];
    } else {
      // Default: prefer principal with coords, then any with coords, then any with address text
      target = direcciones.find(d => d.esPrincipal && d.latitud && d.longitud)
        || direcciones.find(d => d.latitud && d.longitud)
        || direcciones.find(d => d.direccionFormateada || d.direccionCompleta)
        || direcciones[0];
    }

    if (!target) return null;

    if (target.latitud && target.longitud) {
      const url = `https://maps.google.com/maps?q=${target.latitud},${target.longitud}&z=15&output=embed`;
      return this.sanitizer.bypassSecurityTrustResourceUrl(url);
    }

    const addressText = target.direccionFormateada || target.direccionCompleta || this.getAddressDisplay(target);
    if (addressText && addressText !== 'Dirección sin especificar') {
      const query = encodeURIComponent(addressText);
      const url = `https://maps.google.com/maps?q=${query}&z=15&output=embed`;
      return this.sanitizer.bypassSecurityTrustResourceUrl(url);
    }

    return null;
  }

  selectAddressForMap(index: number): void {
    this.selectedAddressForMap.set(index);
  }

  getMapAddressLabel(): string {
    const client = this.client();
    if (!client?.direcciones?.length) return '';
    const selectedIdx = this.selectedAddressForMap();
    const addr = selectedIdx >= 0 && selectedIdx < client.direcciones.length
      ? client.direcciones[selectedIdx]
      : (client.direcciones.find(d => d.esPrincipal) || client.direcciones[0]);
    return addr ? this.getAddressDisplay(addr) : '';
  }

  navigateToEquipo(equipoId: number): void {
    this.router.navigate(['/equipos', equipoId]);
  }

  goBack(): void {
    this.router.navigate(['/clientes']);
  }

  editClient(): void {
    const currentClient = this.client();
    if (!currentClient) return;

    // Fill form with current data
    this.clientEditForm.patchValue({
      tipoPersonaId: this.getPersonTypeId(currentClient.tipoPersona),
      tipoDocumentoId: this.getDocumentTypeId(currentClient.tipoDocumento),
      documento: currentClient.documento,
      nombre: currentClient.nombre,
      apellido: currentClient.apellido,
      razonSocial: currentClient.razonSocial,
      sexo: currentClient.sexo
    });

    this.showEditDialog.set(true);
  }

  closeEditDialog(): void {
    this.showEditDialog.set(false);
    this.clientEditForm.reset();
  }

  saveClientEdit(): void {
    if (!this.clientEditForm.valid) return;

    this.isSaving.set(true);
    const formValue = this.clientEditForm.value;

    const updateData: ClientUpdateRequest = {
      nombre: formValue.nombre,
      apellido: formValue.apellido,
      razonSocial: formValue.razonSocial,
      tipoPersonaId: formValue.tipoPersonaId,
      tipoDocumentoId: formValue.tipoDocumentoId,
      documento: formValue.documento,
      sexo: formValue.sexo
    };

    this.clientService.updateClient(this.clientId, updateData).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Cliente actualizado correctamente'
        });
        this.closeEditDialog();
        this.loadClient(this.clientId);
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
  }

  onPersonTypeChange(): void {
    if (this.isNaturalPerson()) {
      this.clientEditForm.get('nombre')?.setValidators([Validators.required]);
      this.clientEditForm.get('apellido')?.setValidators([Validators.required]);
      this.clientEditForm.get('razonSocial')?.clearValidators();
    } else {
      this.clientEditForm.get('razonSocial')?.setValidators([Validators.required]);
      this.clientEditForm.get('nombre')?.clearValidators();
      this.clientEditForm.get('apellido')?.clearValidators();
    }
    this.clientEditForm.get('nombre')?.updateValueAndValidity();
    this.clientEditForm.get('apellido')?.updateValueAndValidity();
    this.clientEditForm.get('razonSocial')?.updateValueAndValidity();
  }

  isNaturalPerson(): boolean {
    const tipoPersonaId = this.clientEditForm.get('tipoPersonaId')?.value;
    return tipoPersonaId === 1; // 1 = Persona Física
  }

  private getPersonTypeId(descripcion: string): number {
    const personType = this.personTypes().find((pt: any) => pt.descripcion === descripcion);
    return personType?.id || 1;
  }

  private getDocumentTypeId(descripcion: string): number {
    const docType = this.documentTypes().find((dt: any) => dt.descripcion === descripcion);
    return docType?.id || 1;
  }

  readonly sexOptions = [
    { label: 'Masculino', value: 'M' },
    { label: 'Femenino', value: 'F' }
  ];

  // Contact management methods
  openAddContactDialog(): void {
    this.isEditingContact.set(false);
    this.contactForm.reset();
    this.showContactDialog.set(true);
  }

  openEditContactDialog(contacto: any, index: number): void {
    this.isEditingContact.set(true);
    this.editingContactIndex.set(index);
    this.contactForm.patchValue({
      tipoContactoId: contacto.tipoContactoId || this.getTipoContactoIdByName(contacto.tipoContacto),
      descripcion: contacto.descripcion
    });
    this.showContactDialog.set(true);
  }

  closeContactDialog(): void {
    this.showContactDialog.set(false);
    this.contactForm.reset();
    this.isEditingContact.set(false);
    this.editingContactIndex.set(-1);
  }

  saveContact(): void {
    if (!this.contactForm.valid) return;

    const currentClient = this.client();
    if (!currentClient) return;

    this.isSaving.set(true);

    const contactos = currentClient.contactos || [];
    const formValue = this.contactForm.value;
    
    const newContacto: ContactoCreateDto = {
      tipoContactoId: formValue.tipoContactoId,
      descripcion: formValue.descripcion
    };

    let updatedContactos: ContactoCreateDto[];
    
    if (this.isEditingContact()) {
      // Edit existing contact
      updatedContactos = contactos.map((c, i) => 
        i === this.editingContactIndex() 
          ? newContacto 
          : { tipoContactoId: c.tipoContactoId || this.getTipoContactoIdByName(c.tipoContacto), descripcion: c.descripcion }
      );
    } else {
      // Add new contact
      updatedContactos = [
        ...contactos.map(c => ({ 
          tipoContactoId: c.tipoContactoId || this.getTipoContactoIdByName(c.tipoContacto), 
          descripcion: c.descripcion 
        })),
        newContacto
      ];
    }

    // Update client with new contacts
    const updateData: ClientUpdateRequest = {
      contactos: updatedContactos
    };

    this.clientService.updateClient(this.clientId, updateData).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: this.isEditingContact() ? 'Contacto actualizado correctamente' : 'Contacto agregado correctamente'
        });
        this.closeContactDialog();
        this.loadClient(this.clientId);
        this.isSaving.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'Error al guardar contacto'
        });
        this.isSaving.set(false);
      }
    });
  }

  deleteContact(contacto: any, index: number): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar el contacto "${contacto.descripcion}"?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        const currentClient = this.client();
        if (!currentClient) return;

        this.isSaving.set(true);

        const contactos = currentClient.contactos || [];
        const updatedContactos = contactos
          .filter((_, i) => i !== index)
          .map(c => ({ 
            tipoContactoId: c.tipoContactoId || this.getTipoContactoIdByName(c.tipoContacto), 
            descripcion: c.descripcion 
          }));

        const updateData: ClientUpdateRequest = {
          contactos: updatedContactos
        };

        this.clientService.updateClient(this.clientId, updateData).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Contacto eliminado correctamente'
            });
            this.loadClient(this.clientId);
            this.isSaving.set(false);
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error.message || 'Error al eliminar contacto'
            });
            this.isSaving.set(false);
          }
        });
      }
    });
  }

  private getTipoContactoIdByName(nombre: string): number {
    const tipo = this.tiposContacto().find(t => t.descripcion === nombre);
    return tipo?.id || 1;
  }

  getTipoContactoLabel(id: number): string {
    const tipo = this.tiposContacto().find(t => t.id === id);
    return tipo?.descripcion || 'Desconocido';
  }

  getAddressDisplay(address: any): string {
    if (address.direccionCompleta) return address.direccionCompleta;
    if (address.direccionFormateada) return address.direccionFormateada;

    const parts = [];
    if (address.calle) parts.push(address.calle);
    if (address.numero) parts.push(address.numero);
    if (address.ciudad) parts.push(address.ciudad);
    if (address.provincia) parts.push(address.provincia);

    return parts.join(', ') || 'Dirección sin especificar';
  }

  // Google Places API
  private async loadGoogleMaps(): Promise<void> {
    if (this.googleMapsLoaded || typeof google !== 'undefined') {
      this.googleMapsLoaded = true;
      return;
    }
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = `https://maps.googleapis.com/maps/api/js?key=${this.googleMapsApiKey}&libraries=places&loading=async&callback=initGoogleMapsDetail`;
      script.async = true;
      script.defer = true;
      (window as any)['initGoogleMapsDetail'] = () => {
        this.googleMapsLoaded = true;
        resolve();
      };
      script.onerror = (err) => reject(err);
      document.head.appendChild(script);
    });
  }

  private async initializeGooglePlaces(): Promise<void> {
    try {
      if (!this.googleMapsLoaded) {
        await this.loadGoogleMaps();
      }
      await new Promise(resolve => setTimeout(resolve, 300));
      if (typeof google === 'undefined' || !google.maps?.places) return;

      this.autocompleteService = new google.maps.places.AutocompleteService();
      this.placesService = new google.maps.places.PlacesService(document.createElement('div'));
      this.sessionToken = new google.maps.places.AutocompleteSessionToken();
    } catch (error) {
      console.error('Error initializing Google Places:', error);
    }
  }

  searchAddress(event: any): void {
    const query = event.query;
    if (!query || query.length < 3 || !this.autocompleteService) {
      this.addressSuggestions.set([]);
      return;
    }
    const request = {
      input: query,
      sessionToken: this.sessionToken,
      componentRestrictions: { country: 'ar' },
      types: ['geocode', 'establishment']
    };
    this.autocompleteService.getPlacePredictions(request, (predictions: any, status: any) => {
      if (status === google.maps.places.PlacesServiceStatus.OK && predictions) {
        this.addressSuggestions.set(predictions);
      } else {
        this.addressSuggestions.set([]);
      }
    });
  }

  onAddressSelected(event: any): void {
    const prediction = event.value || event;
    if (!prediction?.place_id) return;

    this.selectedAddressSuggestion = prediction.description;

    const request = {
      placeId: prediction.place_id,
      fields: ['place_id', 'formatted_address', 'address_components', 'geometry'],
      sessionToken: this.sessionToken
    };

    this.placesService.getDetails(request, (place: any, status: any) => {
      if (status === google.maps.places.PlacesServiceStatus.OK && place) {
        this.processPlaceDetails(place);
        this.sessionToken = new google.maps.places.AutocompleteSessionToken();
        setTimeout(() => { this.selectedAddressSuggestion = ''; }, 100);
      }
    });
  }

  private processPlaceDetails(place: any): void {
    try {
      const addressComponents = place.address_components || [];
      const getComponent = (types: string[]) =>
        addressComponents.find((c: any) => types.some((t: string) => c.types.includes(t)));

      const streetNumber = getComponent(['street_number'])?.long_name || '';
      const route = getComponent(['route'])?.long_name || '';
      const locality = getComponent(['locality', 'administrative_area_level_2'])?.long_name || '';
      const adminArea1 = getComponent(['administrative_area_level_1'])?.long_name || '';
      const country = getComponent(['country'])?.long_name || '';
      const postalCode = getComponent(['postal_code'])?.long_name || '';

      // Preserve extra fields the user may have already entered
      const prev = this.currentAddressData();

      this.currentAddressData.set({
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
        esPrincipal: prev.esPrincipal ?? (this.client()?.direcciones?.length === 0),
        piso: prev.piso,
        departamento: prev.departamento,
        observaciones: prev.observaciones,
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
      });
    } catch (error) {
      console.error('Error processing place:', error);
    }
  }

  openAddAddressDialog(): void {
    this.isEditingAddress.set(false);
    this.editingAddressIndex.set(-1);
    this.currentAddressData.set({ esPrincipal: !(this.client()?.direcciones?.length) });
    this.selectedAddressSuggestion = '';
    this.addressSuggestions.set([]);
    this.showAddressDialog.set(true);
    setTimeout(() => this.initializeGooglePlaces(), 400);
  }

  openEditAddressDialog(address: Address, index: number): void {
    this.isEditingAddress.set(true);
    this.editingAddressIndex.set(index);
    this.currentAddressData.set({ ...address });
    this.selectedAddressSuggestion = address.direccionFormateada || this.getAddressDisplay(address);
    this.addressSuggestions.set([]);
    this.showAddressDialog.set(true);
    setTimeout(() => this.initializeGooglePlaces(), 400);
  }

  closeAddressDialog(): void {
    this.showAddressDialog.set(false);
    this.currentAddressData.set({});
    this.selectedAddressSuggestion = '';
    this.addressSuggestions.set([]);
    this.isEditingAddress.set(false);
    this.editingAddressIndex.set(-1);
  }

  saveAddress(): void {
    const currentClient = this.client();
    if (!currentClient) return;

    const addressData = this.currentAddressData();
    if (!addressData.placeId && !addressData.direccionFormateada) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Por favor, selecciona una dirección del autocompletado'
      });
      return;
    }

    this.isSaving.set(true);
    const newAddress: Address = { ...addressData };
    const direcciones = currentClient.direcciones || [];
    const editingIdx = this.editingAddressIndex();
    let updatedDirecciones: Address[];

    if (this.isEditingAddress()) {
      updatedDirecciones = direcciones.map((d, i) =>
        i === editingIdx
          ? newAddress
          : (newAddress.esPrincipal ? { ...d, esPrincipal: false } : d)
      );
    } else {
      updatedDirecciones = [
        ...direcciones.map(d => newAddress.esPrincipal ? { ...d, esPrincipal: false } : d),
        newAddress
      ];
    }

    const updateData: ClientUpdateRequest = { direcciones: updatedDirecciones };
    this.clientService.updateClient(this.clientId, updateData).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: this.isEditingAddress() ? 'Dirección actualizada correctamente' : 'Dirección agregada correctamente'
        });
        this.selectedAddressForMap.set(-1);
        this.closeAddressDialog();
        this.loadClient(this.clientId);
        this.isSaving.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'Error al guardar dirección'
        });
        this.isSaving.set(false);
      }
    });
  }

  getContactIcon(tipoContacto: string): string {
    const tipo = tipoContacto.toLowerCase();
    if (tipo.includes('email')) return 'pi-envelope';
    if (tipo.includes('celular') || tipo.includes('móvil')) return 'pi-mobile';
    if (tipo.includes('teléfono') || tipo.includes('telefono')) return 'pi-phone';
    if (tipo.includes('whatsapp')) return 'pi-whatsapp';
    return 'pi-info-circle';
  }
}

