import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  } else {
    router.navigate(['/login']);
    return false;
  }
};

export const loginGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    const rol = authService.user()?.rol;
    const destino = rol === 'PROPIETARIO' ? '/dashboard' : '/servicios';
    router.navigate([destino]);
    return false;
  } else {
    return true;
  }
};

// Solo PROPIETARIO
export const propietarioGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }
  if (authService.user()?.rol === 'PROPIETARIO') {
    return true;
  }
  router.navigate(['/servicios']);
  return false;
};

// PROPIETARIO o ADMINISTRATIVO
export const propietarioAdminGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }
  const rol = authService.user()?.rol;
  if (rol === 'PROPIETARIO' || rol === 'ADMINISTRATIVO') {
    return true;
  }
  router.navigate(['/servicios']);
  return false;
};
