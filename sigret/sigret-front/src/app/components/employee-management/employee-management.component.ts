import { Component, OnInit, signal, computed, inject, ChangeDetectionStrategy, effect, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
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

import { EmployeeService } from '../../services/employee.service';
import { 
  EmployeeCreateRequest, 
  EmployeeUpdateRequest, 
  EmployeeListResponse,
  EmployeeType,
  PersonType,
  DocumentType,
  EmployeeListDto,
  EmployeeResponse,
  Address,
  GooglePlacesData
} from '../../models/employee.model';
import { environment } from '../../../environments/environment';

// Google Maps type declarations
declare const google: any;

@Component({
  selector: 'app-employee-management',
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
  templateUrl: './employee-management.component.html',
  styleUrls: ['./employee-management.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EmployeeManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  public readonly employeeService = inject(EmployeeService);

  // Signals
  public readonly employees = signal<EmployeeListDto[]>([]);
  public readonly employeeTypes = signal<EmployeeType[]>([]);
  public readonly personTypes = signal<PersonType[]>([]);
  public readonly documentTypes = signal<DocumentType[]>([]);
  public readonly userRoles = signal<{ label: string; value: string }[]>([]);
  public readonly isLoading = signal(false);
  public readonly isSaving = signal(false);
  public readonly showEmployeeDialog = signal(false);
  public readonly showEmployeeDetailsDialog = signal(false);
  public readonly isEditMode = signal(false);
  public readonly selectedEmployee = signal<EmployeeListDto | null>(null);
  public readonly employeeDetails = signal<EmployeeResponse | null>(null);
  public readonly isLoadingDetails = signal(false);
  public readonly totalRecords = signal(0);
  public readonly currentPage = signal(0);
  public readonly pageSize = 10;
  
  // Address management signals
  public readonly addresses = signal<Address[]>([]);
  public readonly currentAddress = signal<Address>({});
  public readonly isAddingAddress = signal(false);
  private readonly initialAddresses = signal<Address[]>([]);
  
  // Flag para evitar loop infinito en lazy load
  private isLoadingData = false;
  
  // Filtros
  public filterActivo = signal<boolean | null>(null);
  public filterBusqueda = signal<string>('');
  public readonly statusOptions = [
    { label: 'Todos', value: null },
    { label: 'Activos', value: true },
    { label: 'Inactivos', value: false }
  ];

  // Computed signals
  public readonly totalEmployees = computed(() => this.totalRecords());
  public readonly activeEmployees = computed(() => this.employees().filter(e => e.activo).length);
  public readonly employeesWithUsers = computed(() => this.employees().filter(e => e.tieneUsuario).length);
  
  // Check if addresses have changed
  public readonly addressesChanged = computed(() => {
    const current = this.addresses();
    const initial = this.initialAddresses();
    return JSON.stringify(current) !== JSON.stringify(initial);
  });
  
  // Can save if form is valid OR addresses changed (for edit mode)
  public readonly canSaveEmployee = computed(() => {
    const isEdit = this.isEditMode();
    const formValid = this.employeeForm.valid;
    const formDirty = this.employeeForm.dirty;
    const addressChanged = this.addressesChanged();
    
    console.log('🔍 canSaveEmployee check:', {
      isEdit,
      formValid,
      formDirty,
      addressChanged,
      currentAddresses: this.addresses().length,
      initialAddresses: this.initialAddresses().length
    });
    
    if (!isEdit) {
      // Create mode: form must be valid
      return formValid;
    } else {
      // Edit mode: form must be valid AND something changed
      const canSave = formValid && (formDirty || addressChanged);
      console.log('✅ Can save in edit mode?', canSave);
      return canSave;
    }
  });

  // Form
  public employeeForm: FormGroup;

  // Sex options
  public readonly sexOptions = [
    { label: 'Masculino', value: 'M' },
    { label: 'Femenino', value: 'F' }
  ];
  
  // Google Places - Classic Autocomplete API (estable y funcional)
  @ViewChild('addressInput') addressInput?: ElementRef<HTMLInputElement>;
  private placeAutocomplete: any = null;
  private googleMapsApiKey: string = environment.googleMapsApiKey;
  private googleMapsLoaded = false;
  private scrollListener: (() => void) | null = null;

  constructor() {
    this.employeeForm = this.createEmployeeForm();
  }

  ngOnInit(): void {
    // NO llamar a loadEmployees() aquí porque el p-table con [lazy]="true" 
    // lo disparará automáticamente cuando se inicialice
    this.loadEmployeeTypes();
    this.loadPersonTypes();
    this.loadDocumentTypes();
    this.userRoles.set(this.employeeService.getUserRoles());
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

  private createEmployeeForm(): FormGroup {
    return this.fb.group({
      tipoEmpleadoId: [null, Validators.required],
      tipoPersonaId: [1, Validators.required], // 1 = Persona Física por defecto
      tipoDocumentoId: [1, Validators.required], // 1 = DNI por defecto
      documento: ['', [Validators.required, Validators.minLength(6)]],
        nombre: [''],
        apellido: [''],
        razonSocial: [''],
      sexo: [''],
      rolUsuario: ['TECNICO', Validators.required], // Solo para crear
      usernamePersonalizado: [''], // Opcional
      passwordPersonalizada: [''], // Opcional
      activo: [true] // Solo para editar
    });
  }

  loadEmployees(event?: any): void {
    // PREVENIR LOOP INFINITO: Si ya está cargando, no hacer nada
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
      activo: this.filterActivo(),
      busqueda: this.filterBusqueda() || undefined,
      sort: 'id,DESC'
    };

    this.employeeService.getEmployees(filters).subscribe({
      next: (response: EmployeeListResponse) => {
        this.employees.set(response.content);
        this.totalRecords.set(response.totalElements);
        this.isLoading.set(false);
        this.isLoadingData = false;
      },
      error: (error) => {
        console.error('Error al cargar empleados:', error);
        
        let errorMessage = 'Error al cargar empleados';
        
        if (error.status === 403) {
          errorMessage = 'No tienes permisos para acceder a la gestión de empleados. Contacta al administrador.';
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
    // Disparar el evento lazy load manualmente cuando cambian los filtros
    this.loadEmployees({ first: 0, rows: this.pageSize });
  }

  loadEmployeeTypes(): void {
    this.employeeService.getEmployeeTypes().subscribe({
      next: (types) => {
        this.employeeTypes.set(types);
      },
      error: (error) => {
        console.error('Error loading employee types:', error);
        // No mostrar error al usuario, estos son datos de catálogo opcionales
        this.employeeTypes.set([]);
      }
    });
  }

  loadPersonTypes(): void {
    this.employeeService.getPersonTypes().subscribe({
      next: (types) => {
        this.personTypes.set(types);
      },
      error: (error) => {
        console.error('Error loading person types:', error);
        // No mostrar error al usuario, estos son datos de catálogo opcionales
        this.personTypes.set([]);
      }
    });
  }

  loadDocumentTypes(): void {
    this.employeeService.getDocumentTypes().subscribe({
      next: (types) => {
        this.documentTypes.set(types);
      },
      error: (error) => {
        console.error('Error loading document types:', error);
        // No mostrar error al usuario, estos son datos de catálogo opcionales
        this.documentTypes.set([]);
      }
    });
  }

  openCreateDialog(): void {
    this.isEditMode.set(false);
    this.selectedEmployee.set(null);
    this.addresses.set([]); // Reset addresses
    this.initialAddresses.set([]); // Save initial state
    this.employeeForm.reset({
      tipoEmpleadoId: null,
      tipoPersonaId: 1,
      tipoDocumentoId: 1,
      documento: '',
        nombre: '',
        apellido: '',
        razonSocial: '',
      sexo: '',
      rolUsuario: 'TECNICO',
      usernamePersonalizado: '',
      passwordPersonalizada: '',
      activo: true
    });
    this.showEmployeeDialog.set(true);
  }

  openEditDialog(employee: EmployeeListDto): void {
    this.isEditMode.set(true);
    this.selectedEmployee.set(employee);
    
    // Load full employee details to get addresses
    this.employeeService.getEmployeeById(employee.id).subscribe({
      next: (employeeDetails) => {
        const currentAddresses = employeeDetails.direcciones || [];
        this.addresses.set(currentAddresses);
        this.initialAddresses.set(JSON.parse(JSON.stringify(currentAddresses))); // Deep copy for comparison
        
        this.employeeForm.patchValue({
          nombre: employeeDetails.nombre,
          apellido: employeeDetails.apellido,
          tipoEmpleadoId: employeeDetails.tipoEmpleadoId,
          activo: employeeDetails.activo
        });
        
        this.showEmployeeDialog.set(true);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'Error al cargar los detalles del empleado'
        });
      }
    });
  }

  closeEmployeeDialog(): void {
    this.showEmployeeDialog.set(false);
    this.employeeForm.reset();
    this.selectedEmployee.set(null);
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
  
  // Employee Details Dialog methods
  openEmployeeDetailsDialog(employee: EmployeeListDto): void {
    this.isLoadingDetails.set(true);
    this.showEmployeeDetailsDialog.set(true);
    
    this.employeeService.getEmployeeById(employee.id).subscribe({
      next: (employeeDetails) => {
        this.employeeDetails.set(employeeDetails);
        this.isLoadingDetails.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'Error al cargar los detalles del empleado'
        });
        this.isLoadingDetails.set(false);
        this.showEmployeeDetailsDialog.set(false);
      }
    });
  }
  
  closeEmployeeDetailsDialog(): void {
    this.showEmployeeDetailsDialog.set(false);
    this.employeeDetails.set(null);
  }
  
  openEditDialogFromDetails(): void {
    const details = this.employeeDetails();
    if (!details) return;
    
    // Convert EmployeeResponse to EmployeeListDto for editing
    const employeeListDto: EmployeeListDto = {
      id: details.id || details.empleadoId || 0,
      nombreCompleto: details.nombreCompleto,
      nombre: details.nombre,
      apellido: details.apellido,
      razonSocial: details.razonSocial,
      documento: details.documento,
      tipoDocumento: details.tipoDocumento,
      tipoPersona: details.tipoPersona,
      sexo: details.sexo,
      tipoEmpleado: details.tipoEmpleado,
      tipoEmpleadoId: details.tipoEmpleadoId,
      activo: details.activo || details.empleadoActivo || false,
      tieneUsuario: !!details.usuarioId,
      username: details.username,
      rolUsuario: details.rolUsuario || details.rol,
      usuarioActivo: details.usuarioActivo,
      fechaCreacionUsuario: details.fechaCreacionUsuario || details.fechaCreacion,
      ultimoLogin: details.ultimoLogin,
      direcciones: details.direcciones
    };
    
    this.openEditDialog(employeeListDto);
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
        types: ['address']
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
    
    // Validar que tenga placeId o al menos dirección formateada
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

  onPersonTypeChange(): void {
    if (this.isNaturalPerson()) {
      this.employeeForm.get('nombre')?.setValidators([Validators.required]);
      this.employeeForm.get('apellido')?.setValidators([Validators.required]);
      this.employeeForm.get('razonSocial')?.clearValidators();
    } else {
      this.employeeForm.get('razonSocial')?.setValidators([Validators.required]);
      this.employeeForm.get('nombre')?.clearValidators();
      this.employeeForm.get('apellido')?.clearValidators();
    }
    this.employeeForm.get('nombre')?.updateValueAndValidity();
    this.employeeForm.get('apellido')?.updateValueAndValidity();
    this.employeeForm.get('razonSocial')?.updateValueAndValidity();
  }

  isNaturalPerson(): boolean {
    const tipoPersonaId = this.employeeForm.get('tipoPersonaId')?.value;
    return tipoPersonaId === 1; // 1 = Persona Física
  }

  saveEmployee(): void {
    if (this.employeeForm.valid) {
      this.isSaving.set(true);
      
      const formValue = this.employeeForm.value;
      
      if (this.isEditMode()) {
        const updateData: EmployeeUpdateRequest = {
          nombre: formValue.nombre,
          apellido: formValue.apellido,
          tipoEmpleadoId: formValue.tipoEmpleadoId,
          activo: formValue.activo,
          direcciones: this.addresses().length > 0 ? this.addresses() : undefined
        };
        
        this.employeeService.updateEmployee(this.selectedEmployee()!.id, updateData).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Empleado actualizado correctamente'
            });
            this.closeEmployeeDialog();
            // Recargar con la página actual
            this.loadEmployees({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
            this.isSaving.set(false);
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error.message || 'Error al actualizar empleado'
            });
            this.isSaving.set(false);
          }
        });
      } else {
        const createData: EmployeeCreateRequest = {
          tipoEmpleadoId: formValue.tipoEmpleadoId,
          nombre: formValue.nombre,
          apellido: formValue.apellido,
          razonSocial: formValue.razonSocial,
          tipoPersonaId: formValue.tipoPersonaId,
          tipoDocumentoId: formValue.tipoDocumentoId,
          documento: formValue.documento,
          sexo: formValue.sexo,
          rolUsuario: formValue.rolUsuario,
          usernamePersonalizado: formValue.usernamePersonalizado || undefined,
          passwordPersonalizada: formValue.passwordPersonalizada || undefined,
          direcciones: this.addresses().length > 0 ? this.addresses() : undefined
        };
        
        this.employeeService.createEmployee(createData).subscribe({
          next: (response: EmployeeResponse) => {
            const password = formValue.passwordPersonalizada || formValue.documento;
            this.messageService.add({
              severity: 'success',
              summary: 'Empleado creado exitosamente',
              detail: `Usuario: ${response.username} | Contraseña: ${password}`,
              life: 10000 // Mostrar por 10 segundos
            });
            this.closeEmployeeDialog();
            // Recargar desde la primera página
            this.currentPage.set(0);
            this.loadEmployees({ first: 0, rows: this.pageSize });
            this.isSaving.set(false);
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: error.message || 'Error al crear empleado'
            });
            this.isSaving.set(false);
          }
        });
      }
    }
  }

  activateEmployee(employee: EmployeeListDto): void {
    this.confirmationService.confirm({
      message: `¿Desea activar al empleado "${employee.nombreCompleto}" y su usuario asociado?`,
      header: 'Confirmar Activación',
      icon: 'pi pi-check-circle',
      acceptLabel: 'Sí, activar',
      rejectLabel: 'Cancelar',
      accept: () => {
    this.employeeService.activateEmployee(employee.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
              detail: 'Empleado y usuario activados correctamente'
        });
        // Recargar con la página actual
        this.loadEmployees({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
              detail: error.message || 'Error al activar empleado'
            });
          }
        });
      }
    });
  }

  deactivateEmployee(employee: EmployeeListDto): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea desactivar al empleado "${employee.nombreCompleto}" y su usuario asociado?`,
      header: 'Confirmar Desactivación',
      icon: 'pi pi-ban',
      acceptLabel: 'Sí, desactivar',
      rejectLabel: 'Cancelar',
      accept: () => {
    this.employeeService.deactivateEmployee(employee.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
              detail: 'Empleado y usuario desactivados correctamente'
        });
        // Recargar con la página actual
        this.loadEmployees({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
              detail: error.message || 'Error al desactivar empleado'
            });
          }
        });
      }
    });
  }

  confirmDeleteEmployee(employee: EmployeeListDto): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar permanentemente al empleado "${employee.nombreCompleto}"? Esta acción no se puede deshacer.`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
    this.employeeService.deleteEmployee(employee.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
              detail: 'Empleado eliminado permanentemente'
        });
        // Recargar con la página actual
        this.loadEmployees({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
              detail: error.message || 'Error al eliminar empleado'
            });
          }
        });
      }
    });
  }
}
