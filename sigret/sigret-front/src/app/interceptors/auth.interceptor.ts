import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

// Flag para evitar loop infinito de refresh
let isRefreshing = false;

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
      // 401 Unauthorized = Token expirado o inválido → Intentar refresh
      if (error.status === 401 && authService.getRefreshToken() && !isRefreshing) {
        return handleTokenRefresh(req, next, authService);
      }
      
      // 403 Forbidden = Access Denied (rol insuficiente) → NO intentar refresh
      if (error.status === 403) {
        console.error('🔒 Access Denied:', error.error);
        console.error('URL:', req.url);
        // NO intentar refresh, el token es válido pero el rol es insuficiente
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
  isRefreshing = true;
  
  return authService.refreshTokenAutomatically().pipe(
    switchMap(() => {
      isRefreshing = false;
      
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
      isRefreshing = false;
      
      // Si el refresh falla, limpiar y no reintentar
      authService.clearAuthData();
      return throwError(() => refreshError);
    })
  );
}
