import { Component, OnInit, signal, computed, inject, ChangeDetectionStrategy } from '@angular/core';
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
import { 
  User, 
  UserUpdateRequest, 
  UserRole,
  UserListResponse 
} from '../../models/user.model';
// Los usuarios se crean automáticamente al crear empleados
// Este componente solo gestiona usuarios existentes (editar, activar/desactivar)

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
          label="Ir a Gestión de Empleados" 
          icon="pi pi-users" 
          (click)="navigateToEmployees()"
          severity="secondary">
        </p-button>
      </div>

      <!-- Info Alert -->
      <div class="mb-4">
        <div class="bg-blue-50 p-3 border-round border-1 border-blue-200">
          <p class="text-sm text-blue-900 m-0">
            <i class="pi pi-info-circle mr-2"></i>
            <strong>Nota:</strong> Los usuarios se crean automáticamente al crear empleados. 
            Desde aquí puedes editar roles, activar/desactivar usuarios y cambiar contraseñas.
            Para crear nuevos usuarios, ve a <strong>Gestión de Empleados</strong>.
          </p>
        </div>
      </div>

      <!-- Stats Cards -->
      <div class="grid mb-4">
        <div class="col-12 md:col-4">
          <p-card class="text-center">
            <ng-template pTemplate="content">
              <div class="text-3xl font-bold text-primary">{{ totalUsers() }}</div>
              <div class="text-600">Total Usuarios</div>
            </ng-template>
          </p-card>
        </div>
        <div class="col-12 md:col-4">
          <p-card class="text-center">
            <ng-template pTemplate="content">
              <div class="text-3xl font-bold text-green-500">{{ activeUsers() }}</div>
              <div class="text-600">Usuarios Activos</div>
            </ng-template>
          </p-card>
        </div>
        <div class="col-12 md:col-4">
          <p-card class="text-center">
            <ng-template pTemplate="content">
              <div class="text-3xl font-bold text-orange-500">{{ inactiveUsers() }}</div>
              <div class="text-600">Usuarios Inactivos</div>
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
                        label="Ir a Gestión de Empleados" 
                        icon="pi pi-users"
                        (click)="navigateToEmployees()"
                        severity="secondary">
                      </p-button>
                    </div>
                  </td>
                </tr>
              </ng-template>
            </p-table>
        </ng-template>
      </p-card>

      <!-- Edit User Dialog -->
      <p-dialog 
        header="Editar Usuario"
        [visible]="showUserDialog()"
        (visibleChange)="showUserDialog.set($event)"
        [modal]="true"
        [closable]="true"
        [style]="{ width: '500px' }"
        (onHide)="closeUserDialog()">
        
        <form [formGroup]="userForm" (ngSubmit)="saveUser()">
          <div class="grid">
            @if (selectedUser()) {
              <div class="col-12">
                <div class="bg-blue-50 p-3 border-round mb-3">
                  <p class="text-sm text-blue-900 m-0">
                    <i class="pi pi-user mr-2"></i>
                    Usuario: <strong>{{ selectedUser()!.username }}</strong> - {{ selectedUser()!.nombreCompleto }}
                  </p>
                </div>
              </div>
            }

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
        </form>

        <ng-template pTemplate="footer">
          <div class="flex justify-content-end gap-2">
            <p-button 
              label="Cancelar" 
              severity="secondary"
              text
              (click)="closeUserDialog()">
            </p-button>
            <p-button 
              label="Actualizar"
              [loading]="isSaving()"
              (click)="saveUser()"
              [disabled]="userForm.invalid">
            </p-button>
          </div>
        </ng-template>
      </p-dialog>

      <!-- Change Password Dialog -->
      <p-dialog 
        header="Cambiar Contraseña"
        [visible]="showPasswordDialog()"
        (visibleChange)="showPasswordDialog.set($event)"
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
        </form>

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
              (click)="changePassword()"
              [disabled]="passwordForm.invalid">
            </p-button>
          </div>
        </ng-template>
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
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserManagementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  public readonly userService = inject(UserService);
  // EmployeeService ya no es necesario aquí

  // Signals
  public readonly users = signal<User[]>([]);
  public readonly isLoading = signal(false);
  public readonly isSaving = signal(false);
  public readonly showUserDialog = signal(false);
  public readonly showPasswordDialog = signal(false);
  public readonly isEditMode = signal(false);
  public readonly selectedUser = signal<User | null>(null);
  public readonly totalRecords = signal(0);
  public readonly currentPage = signal(0);
  public readonly pageSize = 10;
  
  // Flag para evitar loop infinito en lazy load
  private isLoadingData = false;

  // Computed signals
  public readonly totalUsers = computed(() => this.totalRecords());
  public readonly activeUsers = computed(() => this.users().filter(u => u.activo).length);
  public readonly inactiveUsers = computed(() => this.users().filter(u => !u.activo).length);

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
    // NO llamar a loadUsers() aquí porque el p-table con [lazy]="true" 
    // lo disparará automáticamente cuando se inicialice
  }

  private createUserForm(): FormGroup {
    // Solo para editar roles y estado, no para crear usuarios
    return this.fb.group({
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
    // PREVENIR LOOP INFINITO: Si ya está cargando, no hacer nada
    if (this.isLoadingData) {
      console.warn('⚠️ Ya hay una carga de usuarios en progreso, ignorando esta llamada');
      return;
    }
    
    this.isLoadingData = true;
    this.isLoading.set(true);
    
    const page = event ? event.first / event.rows : this.currentPage();
    this.currentPage.set(page);

    console.log('🔄 Cargando usuarios - Página:', page);

    this.userService.getUsers(page, this.pageSize).subscribe({
      next: (response: UserListResponse) => {
        console.log('✅ Usuarios cargados:', response.content.length, 'de', response.totalElements);
        this.users.set(response.content);
        this.totalRecords.set(response.totalElements);
        this.isLoading.set(false);
        this.isLoadingData = false;
      },
      error: (error) => {
        console.error('❌ Error al cargar usuarios:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al cargar usuarios: ' + error.message
        });
        this.isLoading.set(false);
        this.isLoadingData = false;
      }
    });
  }

  // Los empleados con usuarios se gestionan desde el módulo de empleados
  // No se necesita listar empleados sin usuario porque todos los empleados tienen usuario

  navigateToEmployees(): void {
    this.router.navigate(['/empleados']);
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

  // Métodos eliminados: ya no se crean usuarios desde aquí
  // Los usuarios se crean automáticamente al crear empleados

  saveUser(): void {
    if (this.userForm.valid) {
      this.isSaving.set(true);
      
      const formValue = this.userForm.value;
      
      // Solo edición de rol y estado (no se crean usuarios desde aquí)
      const updateData: UserUpdateRequest = {
        rol: formValue.rol,
        activo: formValue.activo
      };
      
      this.userService.updateUser(this.selectedUser()!.id, updateData).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Usuario actualizado correctamente'
          });
          this.closeUserDialog();
          // Recargar con la página actual
          this.loadUsers({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
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
        // Recargar con la página actual
        this.loadUsers({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
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
        // Recargar con la página actual
        this.loadUsers({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
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
        // Recargar con la página actual
        this.loadUsers({ first: this.currentPage() * this.pageSize, rows: this.pageSize });
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
