import { Component, OnInit, signal, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, Location } from '@angular/common';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { DividerModule } from 'primeng/divider';
import { ToastModule } from 'primeng/toast';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { PasswordModule } from 'primeng/password';

import { UserService } from '../../services/user.service';
import { UserProfile, ChangePasswordRequest } from '../../models/user.model';

@Component({
  selector: 'app-profile',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    CardModule,
    InputTextModule,
    DividerModule,
    ToastModule,
    ProgressSpinnerModule,
    TagModule,
    DialogModule,
    PasswordModule
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly messageService = inject(MessageService);
  private readonly fb = inject(FormBuilder);
  private readonly location = inject(Location);

  // Signals
  public readonly profile = signal<UserProfile | null>(null);
  public readonly isLoading = signal(false);
  public readonly isChangingPassword = signal(false);
  public readonly showPasswordDialog = signal(false);

  // Form
  public changePasswordForm: FormGroup;

  constructor() {
    this.changePasswordForm = this.createPasswordForm();
  }

  goBack(): void {
    this.location.back();
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  private createPasswordForm(): FormGroup {
    return this.fb.group({
      passwordActual: ['', [Validators.required, Validators.minLength(6)]],
      passwordNueva: ['', [Validators.required, Validators.minLength(6)]],
      passwordNuevaConfirmacion: ['', [Validators.required, Validators.minLength(6)]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  private passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('passwordNueva')?.value;
    const confirmPassword = form.get('passwordNuevaConfirmacion')?.value;
    
    if (newPassword !== confirmPassword) {
      form.get('passwordNuevaConfirmacion')?.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    
    return null;
  }

  loadProfile(): void {
    this.isLoading.set(true);
    this.userService.getMyProfile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al cargar el perfil: ' + error.message
        });
        this.isLoading.set(false);
      }
    });
  }

  openChangePasswordDialog(): void {
    this.changePasswordForm.reset();
    this.showPasswordDialog.set(true);
  }

  closePasswordDialog(): void {
    this.showPasswordDialog.set(false);
    this.changePasswordForm.reset();
  }

  changePassword(): void {
    if (this.changePasswordForm.valid) {
      this.isChangingPassword.set(true);
      
      const data: ChangePasswordRequest = this.changePasswordForm.value;
      
      this.userService.changeMyPassword(data).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Contraseña cambiada',
            detail: 'Tu contraseña ha sido actualizada exitosamente'
          });
          this.closePasswordDialog();
          this.isChangingPassword.set(false);
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'Error al cambiar la contraseña'
          });
          this.isChangingPassword.set(false);
        }
      });
    }
  }

  getRoleLabel(rol: string): string {
    const roles: Record<string, string> = {
      'PROPIETARIO': 'Propietario',
      'ADMINISTRATIVO': 'Administrativo',
      'TECNICO': 'Técnico'
    };
    return roles[rol] || rol;
  }

  getRoleSeverity(rol: string): 'danger' | 'warn' | 'info' | 'secondary' {
    const severities: Record<string, 'danger' | 'warn' | 'info'> = {
      'PROPIETARIO': 'danger',
      'ADMINISTRATIVO': 'warn',
      'TECNICO': 'info'
    };
    return severities[rol] || 'secondary';
  }
}

