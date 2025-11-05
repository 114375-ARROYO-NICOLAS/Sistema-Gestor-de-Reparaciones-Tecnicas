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
import { ClientResponse, ClientUpdateRequest } from '../../models/client.model';
import { TipoContacto, ContactoCreateDto } from '../../models/contact.model';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

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
    ConfirmDialogModule
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

  // Signals
  public readonly client = signal<ClientResponse | null>(null);
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
      this.loadTiposContacto();
      this.loadPersonTypes();
      this.loadDocumentTypes();
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

  getContactIcon(tipoContacto: string): string {
    const tipo = tipoContacto.toLowerCase();
    if (tipo.includes('email')) return 'pi-envelope';
    if (tipo.includes('celular') || tipo.includes('móvil')) return 'pi-mobile';
    if (tipo.includes('teléfono') || tipo.includes('telefono')) return 'pi-phone';
    if (tipo.includes('whatsapp')) return 'pi-whatsapp';
    return 'pi-info-circle';
  }
}

