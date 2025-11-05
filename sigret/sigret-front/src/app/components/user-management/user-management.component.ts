import { Component, OnInit, signal, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { Select } from 'primeng/select';
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
    Select,
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
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss',
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
