import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { catchError } from 'rxjs';
import { throwError } from 'rxjs';

// Flag para evitar mostrar múltiples mensajes de sesión expirada
let sessionExpiredShown = false;

export const sessionExpiryInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const messageService = inject(MessageService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Manejar errores de sesión expirada
      // Excluir las rutas de auth y validación para evitar loops
      const isAuthRoute = req.url.includes('/auth/');
      
      if (error.status === 401 && !isAuthRoute) {
        // Solo mostrar el mensaje una vez
        if (!sessionExpiredShown) {
          sessionExpiredShown = true;
          
          // Mostrar mensaje de sesión expirada
          messageService.add({
            severity: 'warn',
            summary: 'Sesión Expirada',
            detail: 'Su sesión ha expirado. Por favor, inicie sesión nuevamente.',
            life: 3000
          });

          // Redirigir al login después de un breve delay
          setTimeout(() => {
            sessionExpiredShown = false; // Resetear el flag
            router.navigate(['/login']);
          }, 1500);
        }
      }

      return throwError(() => error);
    })
  );
};
