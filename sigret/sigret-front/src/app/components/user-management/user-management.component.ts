import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { PasswordModule } from 'primeng/password';
import { TagModule } from 'primeng/tag';
import { ToolbarModule } from 'primeng/toolbar';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { BadgeModule } from 'primeng/badge';

import { UserService } from '../../services/user.service';
import { EmployeeService } from '../../services/employee.service';
import { 
  User, 
  UserCreateRequest, 
  UserUpdateRequest, 
  UserRole,
  UserListResponse 
} from '../../models/user.model';
import { EmployeeWithoutUser } from '../../models/employee.model';

@Component({
  selector: 'app-user-management',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    TableModule,
    DialogModule,
    InputTextModule,
    SelectModule,
    PasswordModule,
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
    <div class="user-management-container">
      <!-- Header -->
      <div class="flex justify-content-between align-items-center mb-4">
        <div>
          <h2 class="text-2xl font-bold text-900 m-0">Gestión de Usuarios</h2>
          <p class="text-600 mt-2">Administra los usuarios del sistema y sus permisos</p>
        </div>
        <p-button 
          label="Nuevo Usuario" 
          icon="pi pi-plus" 
          (click)="openCreateDialog()"
          severity="primary">
        </p-button>
      </div>

      <!-- Stats Cards -->
      <div class="grid mb-4">
        <div class="col-12 md:col-3">
          <p-card class="text-center">
            <ng-template pTemplate="content">
              <div class="text-3xl font-bold text-primary">{{ totalUsers() }}</div>
              <div class="text-600">Total Usuarios</div>
            </ng-template>
          </p-card>
        </div>
        <div class="col-12 md:col-3">
          <p-card class="text-center">
            <ng-template pTemplate="content">
              <div class="text-3xl font-bold text-green-500">{{ activeUsers() }}</div>
              <div class="text-600">Usuarios Activos</div>
            </ng-template>
          </p-card>
        </div>
        <div class="col-12 md:col-3">
          <p-card class="text-center">
            <ng-template pTemplate="content">
              <div class="text-3xl font-bold text-orange-500">{{ inactiveUsers() }}</div>
              <div class="text-600">Usuarios Inactivos</div>
            </ng-template>
          </p-card>
        </div>
        <div class="col-12 md:col-3">
          <p-card class="text-center">
            <ng-template pTemplate="content">
              <div class="text-3xl font-bold text-purple-500">{{ availableEmployees() }}</div>
              <div class="text-600">Empleados Disponibles</div>
            </ng-template>
          </p-card>
        </div>
      </div>

      <!-- Users Table -->
      <p-card>
        <ng-template pTemplate="header">
          <div class="flex justify-content-between align-items-center p-4">
            <h3 class="text-xl font-semibold m-0">Lista de Usuarios</h3>
            <div class="flex gap-2">
              <p-button 
                icon="pi pi-refresh" 
                severity="secondary"
                text
                (click)="loadUsers()"
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
              [value]="users()" 
              [paginator]="true" 
              [rows]="pageSize"
              [totalRecords]="totalRecords()"
              [lazy]="true"
              (onLazyLoad)="loadUsers($event)"
              [loading]="isLoading()"
              styleClass="p-datatable-sm">
              
              <ng-template pTemplate="header">
                <tr>
                  <th>Usuario</th>
                  <th>Nombre Completo</th>
                  <th>Rol</th>
                  <th>Estado</th>
                  <th>Fecha Creación</th>
                  <th>Último Login</th>
                  <th>Acciones</th>
                </tr>
              </ng-template>

              <ng-template pTemplate="body" let-user>
                <tr>
                  <td>
                    <div class="flex align-items-center gap-2">
                      <i class="pi pi-user text-primary"></i>
                      <span class="font-medium">{{ user.username }}</span>
                    </div>
                  </td>
                  <td>{{ user.nombreCompleto }}</td>
                  <td>
                    <p-tag 
                      [value]="userService.getRoleDisplayName(user.rol)"
                      [severity]="userService.getRoleColor(user.rol)">
                    </p-tag>
                  </td>
                  <td>
                    <p-tag 
                      [value]="user.activo ? 'Activo' : 'Inactivo'"
                      [severity]="user.activo ? 'success' : 'danger'">
                    </p-tag>
                  </td>
                  <td>{{ formatDate(user.fechaCreacion) }}</td>
                  <td>{{ user.ultimoLogin ? formatDate(user.ultimoLogin) : 'Nunca' }}</td>
                  <td>
                    <div class="flex gap-1">
                      <p-button 
                        icon="pi pi-pencil" 
                        severity="secondary"
                        size="small"
                        text
                        (click)="openEditDialog(user)"
                        title="Editar">
                      </p-button>
                      
                      @if (user.activo) {
                        <p-button 
                          icon="pi pi-ban" 
                          severity="secondary"
                          size="small"
                          text
                          (click)="deactivateUser(user)"
                          title="Desactivar">
                        </p-button>
                      } @else {
                        <p-button 
                          icon="pi pi-check" 
                          severity="success"
                          size="small"
                          text
                          (click)="activateUser(user)"
                          title="Activar">
                        </p-button>
                      }
                      
                      <p-button 
                        icon="pi pi-key" 
                        severity="info"
                        size="small"
                        text
                        (click)="openChangePasswordDialog(user)"
                        title="Cambiar Contraseña">
                      </p-button>
                      
                      <p-button 
                        icon="pi pi-trash" 
                        severity="danger"
                        size="small"
                        text
                        (click)="confirmDeleteUser(user)"
                        title="Eliminar">
                      </p-button>
                    </div>
                  </td>
                </tr>
              </ng-template>

              <ng-template pTemplate="emptymessage">
                <tr>
                  <td colspan="7" class="text-center p-4">
                    <div class="flex flex-column align-items-center gap-3">
                      <i class="pi pi-users text-4xl text-400"></i>
                      <div>
                        <h4 class="text-900 font-semibold">No hay usuarios registrados</h4>
                        <p class="text-600">Crea el primer usuario del sistema</p>
                      </div>
                      <p-button 
                        label="Crear Usuario" 
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

      <!-- Create/Edit User Dialog -->
      <p-dialog 
        [header]="isEditMode() ? 'Editar Usuario' : 'Crear Usuario'"
        [visible]="showUserDialog()"
        [modal]="true"
        [closable]="true"
        [style]="{ width: '600px' }"
        (onHide)="closeUserDialog()">
        
        <form [formGroup]="userForm" (ngSubmit)="saveUser()">
          <div class="grid">
            <div class="col-12">
              <label for="empleado" class="block text-900 font-medium mb-2">Empleado *</label>
              <p-select
                id="empleado"
                formControlName="empleadoId"
                [options]="availableEmployeesList()"
                optionLabel="nombreCompleto"
                optionValue="id"
                placeholder="Seleccionar empleado"
                class="w-full">
              </p-select>
            </div>

            <div class="col-12">
              <label for="username" class="block text-900 font-medium mb-2">Nombre de Usuario *</label>
              <div class="p-inputgroup">
                <input 
                  id="username"
                  type="text"
                  pInputText
                  formControlName="username"
                  placeholder="Ingrese el nombre de usuario"
                  class="w-full">
                <p-button 
                  icon="pi pi-refresh"
                  severity="secondary"
                  (click)="generateUsername()"
                  title="Generar automáticamente">
                </p-button>
              </div>
              @if (userForm.get('username')?.errors?.['usernameTaken']) {
                <small class="text-red-500">Este nombre de usuario ya está en uso</small>
              }
            </div>

            <div class="col-12">
              <label for="password" class="block text-900 font-medium mb-2">
                {{ isEditMode() ? 'Nueva Contraseña' : 'Contraseña' }} 
                {{ isEditMode() ? '(opcional)' : '*' }}
              </label>
              <p-password
                id="password"
                formControlName="password"
                [placeholder]="isEditMode() ? 'Dejar vacío para mantener actual' : 'Ingrese la contraseña'"
                [feedback]="true"
                [toggleMask]="true"
                class="w-full">
              </p-password>
            </div>

            <div class="col-12">
              <label for="rol" class="block text-900 font-medium mb-2">Rol *</label>
              <p-select
                id="rol"
                formControlName="rol"
                [options]="userRoles"
                optionLabel="label"
                optionValue="value"
                placeholder="Seleccionar rol"
                class="w-full">
              </p-select>
            </div>

            <div class="col-12">
              <p-divider></p-divider>
              <div class="flex align-items-center gap-2">
                <input 
                  type="checkbox" 
                  id="activo"
                  formControlName="activo"
                  class="mr-2">
                <label for="activo" class="text-900 font-medium">Usuario activo</label>
              </div>
            </div>
          </div>

          <ng-template pTemplate="footer">
            <div class="flex justify-content-end gap-2">
              <p-button 
                label="Cancelar" 
                severity="secondary"
                text
                (click)="closeUserDialog()">
              </p-button>
              <p-button 
                [label]="isEditMode() ? 'Actualizar' : 'Crear'"
                [loading]="isSaving()"
                type="submit"
                [disabled]="userForm.invalid">
              </p-button>
            </div>
          </ng-template>
        </form>
      </p-dialog>

      <!-- Change Password Dialog -->
      <p-dialog 
        header="Cambiar Contraseña"
        [visible]="showPasswordDialog()"
        [modal]="true"
        [closable]="true"
        [style]="{ width: '400px' }"
        (onHide)="closePasswordDialog()">
        
        <form [formGroup]="passwordForm" (ngSubmit)="changePassword()">
          <div class="grid">
            <div class="col-12">
              <label class="block text-900 font-medium mb-2">Usuario</label>
              <div class="p-inputgroup">
                <i class="pi pi-user p-inputgroup-addon"></i>
                <input 
                  type="text"
                  pInputText
                  [value]="selectedUser()?.username"
                  readonly
                  class="w-full">
              </div>
            </div>

            <div class="col-12">
              <label for="newPassword" class="block text-900 font-medium mb-2">Nueva Contraseña *</label>
              <p-password
                id="newPassword"
                formControlName="newPassword"
                placeholder="Ingrese la nueva contraseña"
                [feedback]="true"
                [toggleMask]="true"
                class="w-full">
              </p-password>
            </div>

            <div class="col-12">
              <label for="confirmPassword" class="block text-900 font-medium mb-2">Confirmar Contraseña *</label>
              <p-password
                id="confirmPassword"
                formControlName="confirmPassword"
                placeholder="Confirme la nueva contraseña"
                [toggleMask]="true"
                class="w-full">
              </p-password>
              @if (passwordForm.get('confirmPassword')?.errors?.['mismatch']) {
                <small class="text-red-500">Las contraseñas no coinciden</small>
              }
            </div>
          </div>

          <ng-template pTemplate="footer">
            <div class="flex justify-content-end gap-2">
              <p-button 
                label="Cancelar" 
                severity="secondary"
                text
                (click)="closePasswordDialog()">
              </p-button>
              <p-button 
                label="Cambiar Contraseña"
                [loading]="isSaving()"
                type="submit"
                [disabled]="passwordForm.invalid">
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
    .user-management-container {
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
export class UserManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  public readonly userService = inject(UserService);
  private readonly employeeService = inject(EmployeeService);

  // Signals
  public readonly users = signal<User[]>([]);
  public readonly availableEmployeesList = signal<EmployeeWithoutUser[]>([]);
  public readonly isLoading = signal(false);
  public readonly isSaving = signal(false);
  public readonly showUserDialog = signal(false);
  public readonly showPasswordDialog = signal(false);
  public readonly isEditMode = signal(false);
  public readonly selectedUser = signal<User | null>(null);
  public readonly totalRecords = signal(0);
  public readonly currentPage = signal(0);
  public readonly pageSize = 10;

  // Computed signals
  public readonly totalUsers = computed(() => this.users().length);
  public readonly activeUsers = computed(() => this.users().filter(u => u.activo).length);
  public readonly inactiveUsers = computed(() => this.users().filter(u => !u.activo).length);
  public readonly availableEmployees = computed(() => this.availableEmployeesList().length);

  // Forms
  public userForm: FormGroup;
  public passwordForm: FormGroup;

  // User roles for dropdown
  public readonly userRoles = [
    { label: 'Propietario', value: UserRole.PROPIETARIO },
    { label: 'Administrativo', value: UserRole.ADMINISTRATIVO },
    { label: 'Técnico', value: UserRole.TECNICO }
  ];

  constructor() {
    this.userForm = this.createUserForm();
    this.passwordForm = this.createPasswordForm();
  }

  ngOnInit(): void {
    this.loadUsers();
    this.loadAvailableEmployees();
  }

  private createUserForm(): FormGroup {
    return this.fb.group({
      empleadoId: [null, Validators.required],
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      password: ['', [Validators.minLength(6)]],
      rol: [UserRole.TECNICO, Validators.required],
      activo: [true]
    });
  }

  private createPasswordForm(): FormGroup {
    return this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  private passwordMatchValidator(form: FormGroup) {
    const password = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ mismatch: true });
    } else if (confirmPassword?.errors?.['mismatch']) {
      delete confirmPassword.errors['mismatch'];
      if (Object.keys(confirmPassword.errors).length === 0) {
        confirmPassword.setErrors(null);
      }
    }
    
    return null;
  }

  loadUsers(event?: any): void {
    this.isLoading.set(true);
    
    const page = event ? event.first / event.rows : 0;
    this.currentPage.set(page);

    this.userService.getUsers(page, this.pageSize).subscribe({
      next: (response: UserListResponse) => {
        this.users.set(response.content);
        this.totalRecords.set(response.totalElements);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al cargar usuarios: ' + error.message
        });
        this.isLoading.set(false);
      }
    });
  }

  loadAvailableEmployees(): void {
    this.employeeService.getEmployeesWithoutUser().subscribe({
      next: (employees) => {
        this.availableEmployeesList.set(employees);
      },
      error: (error) => {
        console.error('Error loading available employees:', error);
        // Don't show error to user as this is not critical
      }
    });
  }

  openCreateDialog(): void {
    this.isEditMode.set(false);
    this.selectedUser.set(null);
    this.userForm.reset({
      rol: UserRole.TECNICO,
      activo: true
    });
    this.showUserDialog.set(true);
  }

  openEditDialog(user: User): void {
    this.isEditMode.set(true);
    this.selectedUser.set(user);
    this.userForm.patchValue({
      empleadoId: user.empleadoId,
      username: user.username,
      password: '',
      rol: user.rol,
      activo: user.activo
    });
    this.showUserDialog.set(true);
  }

  closeUserDialog(): void {
    this.showUserDialog.set(false);
    this.userForm.reset();
    this.selectedUser.set(null);
    this.isEditMode.set(false);
  }

  generateUsername(): void {
    const empleadoId = this.userForm.get('empleadoId')?.value;
    if (empleadoId) {
      const employee = this.availableEmployeesList().find(e => e.id === empleadoId);
      if (employee) {
        // Extract first and last name from employee name
        const nameParts = employee.nombreCompleto.split(' ');
        const firstName = nameParts[0] || '';
        const lastName = nameParts.slice(1).join(' ') || '';
        
        const generatedUsername = this.userService.generateUsername(firstName, lastName);
        this.userForm.patchValue({ username: generatedUsername });
        
        // Check availability
        this.checkUsernameAvailability(generatedUsername);
      }
    }
  }

  checkUsernameAvailability(username: string): void {
    if (username && username.length >= 3) {
      this.userService.checkUsernameAvailability(username).subscribe({
        next: (available) => {
          if (!available) {
            this.userForm.get('username')?.setErrors({ usernameTaken: true });
          } else {
            const errors = this.userForm.get('username')?.errors;
            if (errors?.['usernameTaken']) {
              delete errors['usernameTaken'];
              if (Object.keys(errors).length === 0) {
                this.userForm.get('username')?.setErrors(null);
              } else {
                this.userForm.get('username')?.setErrors(errors);
              }
            }
          }
        }
      });
    }
  }

  saveUser(): void {
    if (this.userForm.valid) {
      this.isSaving.set(true);
      
      const formValue = this.userForm.value;
      
      if (this.isEditMode()) {
        const updateData: UserUpdateRequest = {
          username: formValue.username,
          rol: formValue.rol,
          activo: formValue.activo
        };
        
        // Only include password if provided
        if (formValue.password) {
          updateData.password = formValue.password;
        }
        
        this.userService.updateUser(this.selectedUser()!.id, updateData).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Usuario actualizado correctamente'
            });
            this.closeUserDialog();
            this.loadUsers();
            this.isSaving.set(false);
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Error al actualizar usuario: ' + error.message
            });
            this.isSaving.set(false);
          }
        });
      } else {
        const createData: UserCreateRequest = {
          empleadoId: formValue.empleadoId,
          username: formValue.username,
          password: formValue.password,
          rol: formValue.rol,
          activo: formValue.activo
        };
        
        this.userService.createUser(createData).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Usuario creado correctamente'
            });
            this.closeUserDialog();
            this.loadUsers();
            this.loadAvailableEmployees(); // Refresh available employees
            this.isSaving.set(false);
          },
          error: (error) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Error al crear usuario: ' + error.message
            });
            this.isSaving.set(false);
          }
        });
      }
    }
  }

  activateUser(user: User): void {
    this.userService.activateUser(user.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Usuario activado correctamente'
        });
        this.loadUsers();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al activar usuario: ' + error.message
        });
      }
    });
  }

  deactivateUser(user: User): void {
    this.userService.deactivateUser(user.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Usuario desactivado correctamente'
        });
        this.loadUsers();
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al desactivar usuario: ' + error.message
        });
      }
    });
  }

  openChangePasswordDialog(user: User): void {
    this.selectedUser.set(user);
    this.passwordForm.reset();
    this.showPasswordDialog.set(true);
  }

  closePasswordDialog(): void {
    this.showPasswordDialog.set(false);
    this.passwordForm.reset();
    this.selectedUser.set(null);
  }

  changePassword(): void {
    if (this.passwordForm.valid && this.selectedUser()) {
      this.isSaving.set(true);
      
      const newPassword = this.passwordForm.get('newPassword')?.value;
      
      this.userService.changePassword(this.selectedUser()!.id, newPassword).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Contraseña cambiada correctamente'
          });
          this.closePasswordDialog();
          this.isSaving.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Error al cambiar contraseña: ' + error.message
          });
          this.isSaving.set(false);
        }
      });
    }
  }

  confirmDeleteUser(user: User): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar permanentemente el usuario "${user.username}"?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      accept: () => {
        this.deleteUser(user);
      }
    });
  }

  deleteUser(user: User): void {
    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Usuario eliminado correctamente'
        });
        this.loadUsers();
        this.loadAvailableEmployees(); // Refresh available employees
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al eliminar usuario: ' + error.message
        });
      }
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
