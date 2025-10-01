import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);

  // No interceptar requests de autenticación
  if (req.url.includes('/auth/')) {
    return next(req);
  }

  // Agregar token a las requests autorizadas
  const token = authService.getToken();
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Si el error es 401 (Unauthorized) y tenemos un refresh token
      if (error.status === 401 && authService.getRefreshToken()) {
        return handleTokenRefresh(req, next, authService);
      }
      
      return throwError(() => error);
    })
  );
};

function handleTokenRefresh(
  req: HttpRequest<unknown>, 
  next: HttpHandlerFn, 
  authService: AuthService
) {
  return authService.refreshTokenAutomatically().pipe(
    switchMap(() => {
      // Después de refrescar el token, reintentar la request original
      const newToken = authService.getToken();
      if (newToken) {
        const newReq = req.clone({
          setHeaders: {
            Authorization: `Bearer ${newToken}`
          }
        });
        return next(newReq);
      }
      return throwError(() => new Error('No se pudo obtener nuevo token'));
    }),
    catchError((refreshError) => {
      // Si el refresh falla, el AuthService ya maneja la limpieza y redirección
      return throwError(() => refreshError);
    })
  );
}
