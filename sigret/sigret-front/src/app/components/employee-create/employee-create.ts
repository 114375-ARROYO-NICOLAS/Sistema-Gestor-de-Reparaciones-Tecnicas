import { Component, OnInit, signal, inject, ChangeDetectionStrategy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { StepperModule } from 'primeng/stepper';
import { BadgeModule } from 'primeng/badge';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { EmployeeService } from '../../services/employee.service';
import {
  EmployeeCreateRequest,
  EmployeeType,
  PersonType,
  DocumentType,
  EmployeeResponse,
  Address
} from '../../models/employee.model';
import { TipoContacto, ContactoCreateDto } from '../../models/contact.model';
import { environment } from '../../../environments/environment';

// Google Maps type declarations
declare const google: any;

@Component({
  selector: 'app-employee-create',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    ButtonModule,
    CardModule,
    InputTextModule,
    Select,
    TagModule,
    StepperModule,
    BadgeModule,
    ToastModule
  ],
  templateUrl: './employee-create.html',
  styleUrl: './employee-create.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class EmployeeCreateComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  public readonly employeeService = inject(EmployeeService);

  // Signals
  public readonly employeeTypes = signal<EmployeeType[]>([]);
  public readonly personTypes = signal<PersonType[]>([]);
  public readonly documentTypes = signal<DocumentType[]>([]);
  public readonly tiposContacto = signal<TipoContacto[]>([]);
  public readonly userRoles = signal<{ label: string; value: string }[]>([]);
  public readonly isSaving = signal(false);

  // Address management
  public readonly addresses = signal<Address[]>([]);
  public readonly currentAddress = signal<Address>({});
  public readonly isAddingAddress = signal(false);

  // Contact management
  public readonly contacts = signal<ContactoCreateDto[]>([]);
  public readonly currentContact = signal<Partial<ContactoCreateDto>>({});
  public readonly isAddingContact = signal(false);

  // Current step indicator
  public readonly currentStep = signal(0);

  // Form
  public employeeForm: FormGroup;

  // Sex options
  public readonly sexOptions = [
    { label: 'Masculino', value: 'M' },
    { label: 'Femenino', value: 'F' }
  ];

  // Google Places
  @ViewChild('addressInput') addressInput?: ElementRef<HTMLInputElement>;
  private placeAutocomplete: any = null;
  private googleMapsApiKey: string = environment.googleMapsApiKey;
  private googleMapsLoaded = false;
  private scrollListener: (() => void) | null = null;

  constructor() {
    this.employeeForm = this.fb.group({
      tipoEmpleadoId: [null, Validators.required],
      tipoPersonaId: [1, Validators.required],
      tipoDocumentoId: [1, Validators.required],
      documento: ['', [Validators.required, Validators.minLength(6)]],
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      razonSocial: [''],
      sexo: [''],
      rolUsuario: ['TECNICO', Validators.required],
      usernamePersonalizado: [''],
      passwordPersonalizada: [''],
      activo: [true]
    });
  }

  ngOnInit(): void {
    this.loadEmployeeTypes();
    this.loadPersonTypes();
    this.loadDocumentTypes();
    this.loadTiposContacto();
    this.userRoles.set(this.employeeService.getUserRoles());
    this.loadGoogleMaps();

    // Apply validations based on default person type
    setTimeout(() => {
      this.onPersonTypeChange();
    });
  }

  private async loadGoogleMaps(): Promise<void> {
    if (this.googleMapsLoaded || typeof google !== 'undefined') {
      this.googleMapsLoaded = true;
      return;
    }

    return new Promise((resolve, reject) => {
      try {
        const script = document.createElement('script');
        script.src = `https://maps.googleapis.com/maps/api/js?key=${this.googleMapsApiKey}&libraries=places&loading=async&callback=initGoogleMapsCreate`;
        script.async = true;
        script.defer = true;

        (window as any).initGoogleMapsCreate = () => {
          this.googleMapsLoaded = true;
          resolve();
        };

        script.onerror = (error) => {
          console.error('Error loading Google Maps API:', error);
          reject(error);
        };

        document.head.appendChild(script);
      } catch (error) {
        console.error('Error loading Google Maps API:', error);
        reject(error);
      }
    });
  }

  loadEmployeeTypes(): void {
    this.employeeService.getEmployeeTypes().subscribe({
      next: (types) => this.employeeTypes.set(types),
      error: (error) => {
        console.error('Error loading employee types:', error);
        this.employeeTypes.set([]);
      }
    });
  }

  loadPersonTypes(): void {
    this.employeeService.getPersonTypes().subscribe({
      next: (types) => this.personTypes.set(types),
      error: (error) => {
        console.error('Error loading person types:', error);
        this.personTypes.set([]);
      }
    });
  }

  loadDocumentTypes(): void {
    this.employeeService.getDocumentTypes().subscribe({
      next: (types) => this.documentTypes.set(types),
      error: (error) => {
        console.error('Error loading document types:', error);
        this.documentTypes.set([]);
      }
    });
  }

  loadTiposContacto(): void {
    this.employeeService.getTiposContacto().subscribe({
      next: (tipos) => this.tiposContacto.set(tipos),
      error: (error) => {
        console.error('Error loading tipos contacto:', error);
        this.tiposContacto.set([]);
      }
    });
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
    return tipoPersonaId === 1;
  }

  // Contact management
  toggleAddingContact(): void {
    this.isAddingContact.set(!this.isAddingContact());
    if (!this.isAddingContact()) {
      this.currentContact.set({});
    }
  }

  addContact(): void {
    const contact = this.currentContact();

    if (!contact.tipoContactoId || !contact.descripcion) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Por favor, completa todos los campos del contacto'
      });
      return;
    }

    const newContact: ContactoCreateDto = {
      tipoContactoId: contact.tipoContactoId,
      descripcion: contact.descripcion
    };

    this.contacts.update(contacts => [...contacts, newContact]);
    this.currentContact.set({});
    this.isAddingContact.set(false);

    this.messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Contacto agregado correctamente'
    });
  }

  removeContact(index: number): void {
    this.contacts.update(contacts => contacts.filter((_, i) => i !== index));
    this.messageService.add({
      severity: 'info',
      summary: 'Información',
      detail: 'Contacto eliminado'
    });
  }

  getTipoContactoLabel(id: number): string {
    const tipo = this.tiposContacto().find(t => t.id === id);
    return tipo?.descripcion || 'Desconocido';
  }

  getContactIcon(tipoContactoId: number): string {
    const tipo = this.getTipoContactoLabel(tipoContactoId).toLowerCase();
    if (tipo.includes('email')) return 'pi-envelope';
    if (tipo.includes('celular') || tipo.includes('móvil')) return 'pi-mobile';
    if (tipo.includes('teléfono') || tipo.includes('telefono')) return 'pi-phone';
    if (tipo.includes('whatsapp')) return 'pi-whatsapp';
    return 'pi-info-circle';
  }

  // Address management
  toggleAddingAddress(): void {
    this.isAddingAddress.set(!this.isAddingAddress());
    if (this.isAddingAddress()) {
      this.currentAddress.set({ esPrincipal: this.addresses().length === 0 });
      setTimeout(() => this.initializeGooglePlaces(), 500);
    } else {
      this.currentAddress.set({});
      this.cleanupGooglePlaces();
      if (this.addressInput?.nativeElement) {
        this.addressInput.nativeElement.value = '';
      }
    }
  }

  private async initializeGooglePlaces(): Promise<void> {
    if (!this.addressInput?.nativeElement) return;

    try {
      if (!this.googleMapsLoaded) {
        await this.loadGoogleMaps();
      }

      await new Promise(resolve => setTimeout(resolve, 300));

      if (typeof google === 'undefined' || !google.maps || !google.maps.places) {
        console.error('Google Maps Places library not available');
        return;
      }

      if (this.placeAutocomplete) {
        try {
          google.maps.event.clearInstanceListeners(this.placeAutocomplete);
          this.placeAutocomplete = null;
        } catch (e) {
          console.warn('Error removing previous autocomplete:', e);
        }
      }

      const inputElement = this.addressInput.nativeElement;
      let sessionToken = new google.maps.places.AutocompleteSessionToken();

      this.placeAutocomplete = new google.maps.places.Autocomplete(inputElement, {
        componentRestrictions: { country: 'ar' },
        fields: ['place_id', 'formatted_address', 'address_components', 'geometry', 'name'],
        sessionToken: sessionToken,
        types: ['geocode', 'establishment']
      });

      this.placeAutocomplete.addListener('place_changed', () => {
        const place = this.placeAutocomplete.getPlace();
        if (!place || !place.place_id) return;
        this.processPlaceDetails(place);
        sessionToken = new google.maps.places.AutocompleteSessionToken();
      });
    } catch (error) {
      console.error('Error initializing Google Places:', error);
    }
  }

  private processPlaceDetails(place: any): void {
    if (!place) return;

    try {
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
    }
  }

  addAddress(): void {
    const address = this.currentAddress();

    if (!address.placeId && !address.direccionFormateada) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Por favor, selecciona una dirección del autocompletado'
      });
      return;
    }

    if (address.esPrincipal) {
      const updatedAddresses = this.addresses().map(addr => ({ ...addr, esPrincipal: false }));
      this.addresses.set(updatedAddresses);
    }

    this.addresses.update(addrs => [...addrs, address]);
    this.currentAddress.set({});
    this.isAddingAddress.set(false);
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

  openInGoogleMaps(address: Address): void {
    if (!address.latitud || !address.longitud) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Esta dirección no tiene coordenadas disponibles'
      });
      return;
    }

    const mapsUrl = `https://www.google.com/maps/search/?api=1&query=${address.latitud},${address.longitud}`;
    window.open(mapsUrl, '_blank');
  }

  private cleanupGooglePlaces(): void {
    this.removeScrollListener();

    if (this.placeAutocomplete) {
      try {
        google.maps.event.clearInstanceListeners(this.placeAutocomplete);
        this.placeAutocomplete = null;
      } catch (e) {
        console.warn('Error cleaning up autocomplete:', e);
      }
    }

    this.removePacContainer();
  }

  private removeScrollListener(): void {
    if (this.scrollListener) {
      const dialogContent = document.querySelector('.p-dialog-content');
      if (dialogContent) {
        dialogContent.removeEventListener('scroll', this.scrollListener);
      }
      this.scrollListener = null;
    }
  }

  private removePacContainer(): void {
    const pacContainers = document.querySelectorAll('.pac-container');
    pacContainers.forEach(container => {
      container.remove();
    });
  }

  // Helper methods for summary
  getPersonTypeLabel(id: number): string {
    const tipo = this.personTypes().find(t => t.id === id);
    return tipo?.descripcion || '-';
  }

  getDocumentTypeLabel(id: number): string {
    const tipo = this.documentTypes().find(t => t.id === id);
    return tipo?.descripcion || '-';
  }

  getEmployeeTypeLabel(id: number): string {
    const tipo = this.employeeTypes().find(t => t.id === id);
    return tipo?.descripcion || '-';
  }

  getUserRoleLabel(value: string): string {
    const role = this.userRoles().find(r => r.value === value);
    return role?.label || '-';
  }

  // Navigation
  goBack(): void {
    this.router.navigate(['/empleados']);
  }

  // Save employee
  saveEmployee(): void {
    if (!this.employeeForm.valid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Por favor, completa todos los campos requeridos'
      });
      return;
    }

    this.isSaving.set(true);
    const formValue = this.employeeForm.value;

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
      direcciones: this.addresses().length > 0 ? this.addresses() : undefined,
      contactos: this.contacts().length > 0 ? this.contacts() : undefined
    };

    this.employeeService.createEmployee(createData).subscribe({
      next: (response: EmployeeResponse) => {
        const password = formValue.passwordPersonalizada || formValue.documento;
        this.messageService.add({
          severity: 'success',
          summary: 'Empleado creado exitosamente',
          detail: `Usuario: ${response.username} | Contraseña: ${password}`,
          life: 10000
        });
        setTimeout(() => {
          this.router.navigate(['/empleados']);
        }, 1000);
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

  // Step tracking
  onStepChange(index: number): void {
    this.currentStep.set(index);
  }
}
