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

import { EmployeeService } from '../../services/employee.service';
import { EmployeeResponse, EmployeeUpdateRequest } from '../../models/employee.model';
import { TipoContacto, ContactoCreateDto } from '../../models/contact.model';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

@Component({
  selector: 'app-employee-detail',
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
  templateUrl: './employee-detail.component.html',
  styleUrls: ['./employee-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService, ConfirmationService]
})
export class EmployeeDetailComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);
  public readonly employeeService = inject(EmployeeService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  // Signals
  public readonly employee = signal<EmployeeResponse | null>(null);
  public readonly isLoading = signal(true);
  public readonly isSaving = signal(false);
  
  // Edit employee
  public readonly showEditDialog = signal(false);
  public readonly employeeTypes = signal<any[]>([]);
  public employeeEditForm: FormGroup;
  
  // Contact management
  public readonly showContactDialog = signal(false);
  public readonly tiposContacto = signal<TipoContacto[]>([]);
  public readonly isEditingContact = signal(false);
  public readonly editingContactIndex = signal<number>(-1);
  public contactForm: FormGroup;
  
  private employeeId: number = 0;

  constructor() {
    this.contactForm = this.fb.group({
      tipoContactoId: [null, Validators.required],
      descripcion: ['', [Validators.required, Validators.maxLength(200)]]
    });
    
    this.employeeEditForm = this.fb.group({
      tipoEmpleadoId: [null, Validators.required],
      nombre: ['', Validators.required],
      apellido: ['', Validators.required],
      activo: [true]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.employeeId = +id;
      this.loadEmployee(this.employeeId);
      this.loadTiposContacto();
      this.loadEmployeeTypes();
    } else {
      this.goBack();
    }
  }

  private loadEmployeeTypes(): void {
    this.employeeService.getEmployeeTypes().subscribe({
      next: (types) => {
        this.employeeTypes.set(types);
      },
      error: (error) => {
        console.error('Error loading employee types:', error);
        this.employeeTypes.set([]);
      }
    });
  }

  private loadTiposContacto(): void {
    this.employeeService.getTiposContacto().subscribe({
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

  private loadEmployee(id: number): void {
    this.isLoading.set(true);
    this.employeeService.getEmployeeById(id).subscribe({
      next: (employee) => {
        this.employee.set(employee);
        this.isLoading.set(false);
        // Force change detection for OnPush strategy
        this.cdr.markForCheck();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'Error al cargar empleado'
        });
        this.isLoading.set(false);
        this.goBack();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/empleados']);
  }

  editEmployee(): void {
    const currentEmployee = this.employee();
    if (!currentEmployee) return;

    // Fill form with current data
    this.employeeEditForm.patchValue({
      tipoEmpleadoId: currentEmployee.tipoEmpleadoId,
      nombre: currentEmployee.nombre,
      apellido: currentEmployee.apellido,
      activo: currentEmployee.activo || currentEmployee.empleadoActivo || false
    });

    this.showEditDialog.set(true);
  }

  closeEditDialog(): void {
    this.showEditDialog.set(false);
    this.employeeEditForm.reset();
  }

  saveEmployeeEdit(): void {
    if (!this.employeeEditForm.valid) return;

    this.isSaving.set(true);
    const formValue = this.employeeEditForm.value;

    const updateData: EmployeeUpdateRequest = {
      nombre: formValue.nombre,
      apellido: formValue.apellido,
      tipoEmpleadoId: formValue.tipoEmpleadoId,
      activo: formValue.activo
    };

    this.employeeService.updateEmployee(this.employeeId, updateData).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Empleado actualizado correctamente'
        });
        this.closeEditDialog();
        this.loadEmployee(this.employeeId);
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
  }

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

    const currentEmployee = this.employee();
    if (!currentEmployee) return;

    this.isSaving.set(true);

    const contactos = currentEmployee.contactos || [];
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

    // Update employee with new contacts
    const updateData: EmployeeUpdateRequest = {
      contactos: updatedContactos
    };

    this.employeeService.updateEmployee(this.employeeId, updateData).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: this.isEditingContact() ? 'Contacto actualizado correctamente' : 'Contacto agregado correctamente'
        });
        this.closeContactDialog();
        this.loadEmployee(this.employeeId);
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
        const currentEmployee = this.employee();
        if (!currentEmployee) return;

        this.isSaving.set(true);

        const contactos = currentEmployee.contactos || [];
        const updatedContactos = contactos
          .filter((_, i) => i !== index)
          .map(c => ({ 
            tipoContactoId: c.tipoContactoId || this.getTipoContactoIdByName(c.tipoContacto), 
            descripcion: c.descripcion 
          }));

        const updateData: EmployeeUpdateRequest = {
          contactos: updatedContactos
        };

        this.employeeService.updateEmployee(this.employeeId, updateData).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Contacto eliminado correctamente'
            });
            this.loadEmployee(this.employeeId);
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

