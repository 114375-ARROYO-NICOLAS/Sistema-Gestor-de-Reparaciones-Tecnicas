import { Injectable, signal, effect } from '@angular/core';
import { AuthService } from './auth.service';
import { interval, Subscription } from 'rxjs';
import { filter, take } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TokenRefreshService {
  private refreshSubscription?: Subscription;
  private readonly REFRESH_CHECK_INTERVAL = 60000; // Verificar cada minuto
  private readonly REFRESH_BEFORE_EXPIRY = 300000; // Renovar 5 minutos antes de expirar

  constructor(private authService: AuthService) {
    // Iniciar el monitoreo de tokens cuando el usuario esté autenticado
    effect(() => {
      if (this.authService.isAuthenticated()) {
        this.startTokenMonitoring();
      } else {
        this.stopTokenMonitoring();
      }
    });
  }

  private startTokenMonitoring(): void {
    this.stopTokenMonitoring(); // Asegurar que no hay múltiples subscripciones

    this.refreshSubscription = interval(this.REFRESH_CHECK_INTERVAL)
      .pipe(
        filter(() => this.authService.isAuthenticated()),
        filter(() => this.shouldRefreshToken())
      )
      .subscribe(() => {
        this.refreshTokenProactively();
      });
  }

  private stopTokenMonitoring(): void {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
      this.refreshSubscription = undefined;
    }
  }

  private shouldRefreshToken(): boolean {
    const token = this.authService.getToken();
    if (!token) return false;

    try {
      // Decodificar el token JWT para obtener la fecha de expiración
      const payload = this.decodeJwtPayload(token);
      const exp = payload.exp * 1000; // Convertir a milisegundos
      const now = Date.now();
      const timeUntilExpiry = exp - now;

      // Renovar si queda menos de 5 minutos para expirar
      return timeUntilExpiry < this.REFRESH_BEFORE_EXPIRY;
    } catch (error) {
      console.error('Error decodificando token:', error);
      return false;
    }
  }

  private refreshTokenProactively(): void {
    if (this.authService.isRefreshing()) {
      return; // Ya se está refrescando
    }

    console.log('Refrescando token proactivamente...');
    
    this.authService.refreshTokenAutomatically().subscribe({
      next: () => {
        console.log('Token refrescado exitosamente');
      },
      error: (error) => {
        console.error('Error al refrescar token proactivamente:', error);
        // El AuthService ya maneja la limpieza y redirección
      }
    });
  }

  private decodeJwtPayload(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      throw new Error('Token JWT inválido');
    }
  }

  // Método público para forzar la renovación
  forceRefresh(): void {
    if (this.authService.isAuthenticated()) {
      this.refreshTokenProactively();
    }
  }
}
