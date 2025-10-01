import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { catchError } from 'rxjs';
import { throwError } from 'rxjs';

export const sessionExpiryInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const messageService = inject(MessageService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Manejar errores de sesión expirada
      if (error.status === 401 && !req.url.includes('/auth/')) {
        // Mostrar mensaje de sesión expirada
        messageService.add({
          severity: 'warn',
          summary: 'Sesión Expirada',
          detail: 'Su sesión ha expirado. Redirigiendo al login...',
          life: 3000
        });

        // Redirigir al login después de un breve delay
        setTimeout(() => {
          router.navigate(['/login']);
        }, 1500);
      }

      return throwError(() => error);
    })
  );
};
