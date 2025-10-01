import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
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
  Employee, 
  EmployeeCreateRequest, 
  EmployeeUpdateRequest, 
  EmployeeListResponse,
  EmployeeType,
  PersonType,
  DocumentType
} from '../../models/employee.model';

@Component({
  selector: 'app-employee-management',
  imports: [
    CommonModule,
    ReactiveFormsModule,
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
  template: `
    <div class="employee-management-container">
      <!-- Header -->
      <div class="flex justify-content-between align-items-center mb-4">
        <div>
          <h2 class="text-2xl font-bold text-900 m-0">Gestión de Empleados</h2>
          <p class="text-600 mt-2">Administra la información de los empleados del sistema</p>
        </div>
        <p-button 
          label="Nuevo Empleado" 
          icon="pi pi-plus" 
          (click)="openCreateDialog()"
          severity="primary">
        </p-button>
      </div>

      <!-- Stats Cards -->
      <div class="grid mb-4">
        <div class="col-12 md:col-4">
          <p-card class="text-center">
            <ng-template pTemplate="content">
              <div class="text-3xl font-bold text-primary">{{ totalEmployees() }}</div>
              <div class="text-600">Total Empleados</div>
            </ng-template>
          </p-card>
        </div>
        <div class="col-12 md:col-4">
          <p-card class="text-center">
            <ng-template pTemplate="content">
              <div class="text-3xl font-bold text-green-500">{{ activeEmployees() }}</div>
              <div class="text-600">Empleados Activos</div>
            </ng-template>
          </p-card>
        </div>
        <div class="col-12 md:col-4">
          <p-card class="text-center">
            <ng-template pTemplate="content">
              <div class="text-3xl font-bold text-orange-500">{{ employeesWithUsers() }}</div>
              <div class="text-600">Con Usuario</div>
            </ng-template>
          </p-card>
        </div>
      </div>

      <!-- Employees Table -->
      <p-card>
        <ng-template pTemplate="header">
          <div class="flex justify-content-between align-items-center p-4">
            <h3 class="text-xl font-semibold m-0">Lista de Empleados</h3>
            <div class="flex gap-2">
              <p-button 
                icon="pi pi-refresh" 
                severity="secondary"
                text
                (click)="loadEmployees()"
                [loading]="isLoading()"
                title="Actualizar">
              </p-button>
            </div>
          </div>
        </ng-template>

        <ng-template pTemplate="content">
          @if (isLoading()) {
            <div class="flex justify-content-center p-4">
              <p-progressSpinner></p-progressSpinner>
            </div>
          } @else {
            <p-table 
              [value]="employees()" 
              [paginator]="true" 
              [rows]="pageSize"
              [totalRecords]="totalRecords()"
              [lazy]="true"
              (onLazyLoad)="loadEmployees($event)"
              [loading]="isLoading()"
              styleClass="p-datatable-sm">
              
              <ng-template pTemplate="header">
                <tr>
                  <th>Nombre Completo</th>
                  <th>Tipo de Empleado</th>
                  <th>Documento</th>
                  <th>Estado</th>
                  <th>Usuario</th>
                  <th>Acciones</th>
                </tr>
              </ng-template>

              <ng-template pTemplate="body" let-employee>
                <tr>
                  <td>
                    <div class="flex align-items-center gap-2">
                      <i class="pi pi-user text-primary"></i>
                      <span class="font-medium">{{ employee.nombreCompleto }}</span>
                    </div>
                  </td>
                  <td>{{ employee.tipoEmpleado.descripcion }}</td>
                  <td>{{ employee.persona.documento }}</td>
                  <td>
                    <p-tag 
                      [value]="employeeService.getStatusDisplayName(employee.activo)"
                      [severity]="employeeService.getStatusColor(employee.activo)">
                    </p-tag>
                  </td>
                  <td>
                    @if (employee.tieneUsuario) {
                      <p-badge value="Sí" severity="success"></p-badge>
                    } @else {
                      <p-badge value="No" severity="secondary"></p-badge>
                    }
                  </td>
                  <td>
                    <div class="flex gap-1">
                      <p-button 
                        icon="pi pi-pencil" 
                        severity="secondary"
                        size="small"
                        text
                        (click)="openEditDialog(employee)"
                        title="Editar">
                      </p-button>
                      
                      @if (employee.activo) {
                        <p-button 
                          icon="pi pi-ban" 
                          severity="secondary"
                          size="small"
                          text
                          (click)="deactivateEmployee(employee)"
                          title="Desactivar">
                        </p-button>
                      } @else {
                        <p-button 
                          icon="pi pi-check" 
                          severity="success"
                          size="small"
                          text
                          (click)="activateEmployee(employee)"
                          title="Activar">
                        </p-button>
                      }
                      
                      <p-button 
                        icon="pi pi-trash" 
                        severity="danger"
                        size="small"
                        text
                        (click)="confirmDeleteEmployee(employee)"
                        title="Eliminar">
                      </p-button>
                    </div>
                  </td>
                </tr>
              </ng-template>

              <ng-template pTemplate="emptymessage">
                <tr>
                  <td colspan="6" class="text-center p-4">
                    <div class="flex flex-column align-items-center gap-3">
                      <i class="pi pi-users text-4xl text-400"></i>
                      <div>
                        <h4 class="text-900 font-semibold">No hay empleados registrados</h4>
                        <p class="text-600">Crea el primer empleado del sistema</p>
                      </div>
                      <p-button 
                        label="Crear Empleado" 
                        icon="pi pi-plus"
                        (click)="openCreateDialog()"
                        severity="primary">
                      </p-button>
                    </div>
                  </td>
                </tr>
              </ng-template>
            </p-table>
          }
        </ng-template>
      </p-card>

      <!-- Create/Edit Employee Dialog -->
      <p-dialog 
        [header]="isEditMode() ? 'Editar Empleado' : 'Crear Empleado'"
        [visible]="showEmployeeDialog()"
        [modal]="true"
        [closable]="true"
        [style]="{ width: '700px' }"
        (onHide)="closeEmployeeDialog()">
        
        <form [formGroup]="employeeForm" (ngSubmit)="saveEmployee()">
          <div class="grid">
            <!-- Employee Type -->
            <div class="col-12 md:col-6">
              <label for="tipoEmpleado" class="block text-900 font-medium mb-2">Tipo de Empleado *</label>
              <p-select
                id="tipoEmpleado"
                formControlName="tipoEmpleadoId"
                [options]="employeeTypes()"
                optionLabel="descripcion"
                optionValue="id"
                placeholder="Seleccionar tipo"
                class="w-full">
              </p-select>
            </div>

            <!-- Document Type -->
            <div class="col-12 md:col-6">
              <label for="tipoDocumento" class="block text-900 font-medium mb-2">Tipo de Documento *</label>
              <p-select
                id="tipoDocumento"
                formControlName="tipoDocumentoId"
                [options]="documentTypes()"
                optionLabel="descripcion"
                optionValue="id"
                placeholder="Seleccionar tipo"
                class="w-full">
              </p-select>
            </div>

            <!-- Document Number -->
            <div class="col-12 md:col-6">
              <label for="documento" class="block text-900 font-medium mb-2">Número de Documento *</label>
              <input 
                id="documento"
                type="text"
                pInputText
                formControlName="documento"
                placeholder="Ingrese el número de documento"
                class="w-full">
            </div>

            <!-- Person Type -->
            <div class="col-12 md:col-6">
              <label for="tipoPersona" class="block text-900 font-medium mb-2">Tipo de Persona *</label>
              <p-select
                id="tipoPersona"
                formControlName="tipoPersonaId"
                [options]="personTypes()"
                optionLabel="descripcion"
                optionValue="id"
                placeholder="Seleccionar tipo"
                (onChange)="onPersonTypeChange()"
                class="w-full">
              </p-select>
            </div>

            <!-- Name fields (for natural person) -->
            <ng-container formGroupName="persona">
              <div class="col-12 md:col-6" *ngIf="isNaturalPerson()">
                <label for="nombre" class="block text-900 font-medium mb-2">Nombre *</label>
                <input 
                  id="nombre"
                  type="text"
                  pInputText
                  formControlName="nombre"
                  placeholder="Ingrese el nombre"
                  class="w-full">
              </div>

              <div class="col-12 md:col-6" *ngIf="isNaturalPerson()">
                <label for="apellido" class="block text-900 font-medium mb-2">Apellido *</label>
                <input 
                  id="apellido"
                  type="text"
                  pInputText
                  formControlName="apellido"
                  placeholder="Ingrese el apellido"
                  class="w-full">
              </div>

              <div class="col-12 md:col-6" *ngIf="isNaturalPerson()">
                <label for="sexo" class="block text-900 font-medium mb-2">Sexo</label>
                <p-select
                  id="sexo"
                  formControlName="sexo"
                  [options]="sexOptions"
                  placeholder="Seleccionar sexo"
                  class="w-full">
                </p-select>
              </div>

              <!-- Company name (for legal person) -->
              <div class="col-12" *ngIf="!isNaturalPerson()">
                <label for="razonSocial" class="block text-900 font-medium mb-2">Razón Social *</label>
                <input 
                  id="razonSocial"
                  type="text"
                  pInputText
                  formControlName="razonSocial"
                  placeholder="Ingrese la razón social"
                  class="w-full">
              </div>
            </ng-container>

            <div class="col-12">
              <p-divider></p-divider>
              <div class="flex align-items-center gap-2">
                <input 
                  type="checkbox" 
                  id="activo"
                  formControlName="activo"
                  class="mr-2">
                <label for="activo" class="text-900 font-medium">Empleado activo</label>
              </div>
            </div>
          </div>

          <ng-template pTemplate="footer">
            <div class="flex justify-content-end gap-2">
              <p-button 
                label="Cancelar" 
                severity="secondary"
                text
                (click)="closeEmployeeDialog()">
              </p-button>
              <p-button 
                [label]="isEditMode() ? 'Actualizar' : 'Crear'"
                [loading]="isSaving()"
                type="submit"
                [disabled]="employeeForm.invalid">
              </p-button>
            </div>
          </ng-template>
        </form>
      </p-dialog>
    </div>

    <!-- Toast and Confirmation Dialog -->
    <p-toast></p-toast>
    <p-confirmDialog></p-confirmDialog>
  `,
  styles: [`
    .employee-management-container {
      padding: 1.5rem;
    }

    .p-datatable-sm .p-datatable-tbody > tr > td {
      padding: 0.5rem;
    }

    .p-dialog-content {
      padding: 1.5rem;
    }
  `]
})
export class EmployeeManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  public readonly employeeService = inject(EmployeeService);

  // Signals
  public readonly employees = signal<Employee[]>([]);
  public readonly employeeTypes = signal<EmployeeType[]>([]);
  public readonly personTypes = signal<PersonType[]>([]);
  public readonly documentTypes = signal<DocumentType[]>([]);
  public readonly isLoading = signal(false);
  public readonly isSaving = signal(false);
  public readonly showEmployeeDialog = signal(false);
  public readonly isEditMode = signal(false);
  public readonly selectedEmployee = signal<Employee | null>(null);
  public readonly totalRecords = signal(0);
  public readonly currentPage = signal(0);
  public readonly pageSize = 10;

  // Computed signals
  public readonly totalEmployees = computed(() => this.employees().length);
  public readonly activeEmployees = computed(() => this.employees().filter(e => e.activo).length);
  public readonly employeesWithUsers = computed(() => this.employees().filter(e => e.tieneUsuario).length);

  // Form
  public employeeForm: FormGroup;

  // Sex options
  public readonly sexOptions = [
    { label: 'Masculino', value: 'M' },
    { label: 'Femenino', value: 'F' }
  ];

  constructor() {
    this.employeeForm = this.createEmployeeForm();
  }

  ngOnInit(): void {
    this.loadEmployees();
    this.loadEmployeeTypes();
    this.loadPersonTypes();
    this.loadDocumentTypes();
  }

  private createEmployeeForm(): FormGroup {
    return this.fb.group({
      tipoEmpleadoId: [null, Validators.required],
      activo: [true],
      persona: this.fb.group({
        tipoPersonaId: [null, Validators.required],
        nombre: [''],
        apellido: [''],
        razonSocial: [''],
        tipoDocumentoId: [null, Validators.required],
        documento: ['', [Validators.required, Validators.minLength(3)]],
        sexo: ['']
      })
    });
  }

  loadEmployees(event?: any): void {
    this.isLoading.set(true);
    
    const page = event ? event.first / event.rows : 0;
    this.currentPage.set(page);

    this.employeeService.getEmployees(page, this.pageSize).subscribe({
      next: (response: EmployeeListResponse) => {
        this.employees.set(response.content);
        this.totalRecords.set(response.totalElements);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al cargar empleados: ' + error.message
        });
        this.isLoading.set(false);
      }
    });
  }

  loadEmployeeTypes(): void {
    this.employeeService.getEmployeeTypes().subscribe({
      next: (types) => {
        this.employeeTypes.set(types);
      },
      error: (error) => {
        console.error('Error loading employee types:', error);
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
      }
    });
  }

  openCreateDialog(): void {
    this.isEditMode.set(false);
    this.selectedEmployee.set(null);
    this.employeeForm.reset({
      activo: true,
      persona: {
        tipoPersonaId: null,
        nombre: '',
        apellido: '',
        razonSocial: '',
        tipoDocumentoId: null,
        documento: '',
        sexo: ''
      }
    });
    this.showEmployeeDialog.set(true);
  }

  openEditDialog(employee: Employee): void {
    this.isEditMode.set(true);
    this.selectedEmployee.set(employee);
    
    this.employeeForm.patchValue({
      tipoEmpleadoId: employee.tipoEmpleado.id,
      activo: employee.activo,
      persona: {
        tipoPersonaId: employee.persona.tipoPersona.id,
        nombre: employee.persona.nombre,
        apellido: employee.persona.apellido,
        razonSocial: employee.persona.razonSocial,
        tipoDocumentoId: employee.persona.tipoDocumento.id,
        documento: employee.persona.documento,
        sexo: employee.persona.sexo
      }
    });
    
    this.showEmployeeDialog.set(true);
  }

  closeEmployeeDialog(): void {
    this.showEmployeeDialog.set(false);
    this.employeeForm.reset();
    this.selectedEmployee.set(null);
    this.isEditMode.set(false);
  }

  onPersonTypeChange(): void {
    const personaForm = this.employeeForm.get('persona');
    if (this.isNaturalPerson()) {
      personaForm?.get('nombre')?.setValidators([Validators.required]);
      personaForm?.get('apellido')?.setValidators([Validators.required]);
      personaForm?.get('razonSocial')?.clearValidators();
    } else {
      personaForm?.get('razonSocial')?.setValidators([Validators.required]);
      personaForm?.get('nombre')?.clearValidators();
      personaForm?.get('apellido')?.clearValidators();
    }
    personaForm?.updateValueAndValidity();
  }

  isNaturalPerson(): boolean {
    const tipoPersonaId = this.employeeForm.get('persona.tipoPersonaId')?.value;
    // Assuming natural person has ID 1, adjust based on your data
    return tipoPersonaId === 1;
  }

  saveEmployee(): void {
    if (this.employeeForm.valid) {
      this.isSaving.set(true);
      
      const formValue = this.employeeForm.value;
      
      if (this.isEditMode()) {
        const updateData: EmployeeUpdateRequest = {
          tipoEmpleadoId: formValue.tipoEmpleadoId,
          activo: formValue.activo,
          persona: {
            nombre: formValue.persona.nombre,
            apellido: formValue.persona.apellido,
            razonSocial: formValue.persona.razonSocial,
            sexo: formValue.persona.sexo
          }
        };
        
        this.employeeService.updateEmployee(this.selectedEmployee()!.id, updateData).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Empleado actualizado correctamente'
            });
            this.closeEmployeeDialog();
            this.loadEmployees();
            this.isSaving.set(false);
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Error al actualizar empleado: ' + error.message
            });
            this.isSaving.set(false);
          }
        });
      } else {
        const createData: EmployeeCreateRequest = {
          tipoEmpleadoId: formValue.tipoEmpleadoId,
          activo: formValue.activo,
          persona: formValue.persona
        };
        
        this.employeeService.createEmployee(createData).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Empleado creado correctamente'
            });
            this.closeEmployeeDialog();
            this.loadEmployees();
            this.isSaving.set(false);
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Error al crear empleado: ' + error.message
            });
            this.isSaving.set(false);
          }
        });
      }
    }
  }

  activateEmployee(employee: Employee): void {
    this.employeeService.activateEmployee(employee.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Empleado activado correctamente'
        });
        this.loadEmployees();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al activar empleado: ' + error.message
        });
      }
    });
  }

  deactivateEmployee(employee: Employee): void {
    this.employeeService.deactivateEmployee(employee.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Empleado desactivado correctamente'
        });
        this.loadEmployees();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al desactivar empleado: ' + error.message
        });
      }
    });
  }

  confirmDeleteEmployee(employee: Employee): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar permanentemente el empleado "${employee.nombreCompleto}"?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.deleteEmployee(employee);
      }
    });
  }

  deleteEmployee(employee: Employee): void {
    this.employeeService.deleteEmployee(employee.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Empleado eliminado correctamente'
        });
        this.loadEmployees();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al eliminar empleado: ' + error.message
        });
      }
    });
  }
}
