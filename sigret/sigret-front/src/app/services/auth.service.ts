import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, catchError, tap, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { 
  LoginRequest, 
  LoginResponse, 
  RefreshTokenRequest, 
  ProfileResponse, 
  TokenValidationResponse,
  ApiError 
} from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_BASE_URL = 'http://localhost:8080/auth';
  
  private readonly tokenSubject = new BehaviorSubject<string | null>(this.getStoredToken());
  private readonly refreshTokenSubject = new BehaviorSubject<string | null>(this.getStoredRefreshToken());
  private readonly userSubject = new BehaviorSubject<ProfileResponse | null>(null);
  
  // Signals para el estado de autenticación
  public readonly token = signal<string | null>(this.tokenSubject.value);
  public readonly refreshTokenSignal = signal<string | null>(this.refreshTokenSubject.value);
  public readonly user = signal<ProfileResponse | null>(this.userSubject.value);
  public readonly isAuthenticated = computed(() => !!this.token());
  public readonly isLoading = signal(false);
  public readonly isRefreshing = signal(false);

  constructor(private http: HttpClient) {
    // NO validar automáticamente para evitar loops
    // La validación se hará cuando el usuario navegue
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    this.isLoading.set(true);
    
    return this.http.post<LoginResponse>(`${this.API_BASE_URL}/login`, credentials)
      .pipe(
        tap(response => {
          this.setTokens(response.token, response.refreshToken);
          this.setUser(response.userInfo);
          this.isLoading.set(false);
        }),
        catchError(error => {
          this.isLoading.set(false);
          return throwError(() => error);
        })
      );
  }

  refreshTokenRequest(refreshRequest: RefreshTokenRequest): Observable<LoginResponse> {
    this.isRefreshing.set(true);
    
    return this.http.post<LoginResponse>(`${this.API_BASE_URL}/refresh`, refreshRequest)
      .pipe(
        tap(response => {
          this.setTokens(response.token, response.refreshToken);
          this.setUser(response.userInfo);
          this.isRefreshing.set(false);
        }),
        catchError(error => {
          this.isRefreshing.set(false);
          this.handleRefreshTokenError();
          return throwError(() => error);
        })
      );
  }

  // Método para renovación automática de token
  refreshTokenAutomatically(): Observable<LoginResponse> {
    const currentRefreshToken = this.getStoredRefreshToken();
    
    if (!currentRefreshToken) {
      this.handleRefreshTokenError();
      return throwError(() => new Error('No hay refresh token disponible'));
    }

    const refreshRequest: RefreshTokenRequest = {
      refreshToken: currentRefreshToken
    };

    return this.refreshTokenRequest(refreshRequest);
  }

  getProfile(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(`${this.API_BASE_URL}/profile`)
      .pipe(
        tap(user => this.setUser(user)),
        catchError(error => {
          this.logout();
          return throwError(() => error);
        })
      );
  }

  validateToken(): Observable<TokenValidationResponse> {
    return this.http.get<TokenValidationResponse>(`${this.API_BASE_URL}/validate`)
      .pipe(
        catchError(error => {
          // NO llamar a logout aquí para evitar loops
          // Solo limpiar datos localmente
          this.clearAuthData();
          return throwError(() => error);
        })
      );
  }

  logout(): Observable<any> {
    return this.http.post(`${this.API_BASE_URL}/logout`, {})
      .pipe(
        tap(() => {
          this.clearAuthData();
        }),
        catchError(error => {
          // Incluso si falla el logout en el servidor, limpiamos los datos localmente
          this.clearAuthData();
          return throwError(() => error);
        })
      );
  }

  private checkTokenValidity(): void {
    const token = this.getStoredToken();
    if (token) {
      // Validar token de manera silenciosa
      this.validateToken().subscribe({
        next: () => {
          // Token válido, cargar perfil del usuario
          this.getProfile().subscribe({
            error: () => {
              // Si falla cargar el perfil, limpiar datos silenciosamente
              this.clearAuthData();
            }
          });
        },
        error: () => {
          // Token inválido, limpiar datos silenciosamente (sin mostrar toast)
          this.clearAuthData();
        }
      });
    }
  }

  private setTokens(token: string, refreshToken: string): void {
    // Access token en sessionStorage (se limpia al cerrar navegador)
    sessionStorage.setItem('auth_token', token);
    
    // Refresh token en localStorage con cifrado básico
    const encryptedRefreshToken = this.encryptToken(refreshToken);
    localStorage.setItem('refresh_token', encryptedRefreshToken);
    
    this.tokenSubject.next(token);
    this.refreshTokenSubject.next(refreshToken);
    this.token.set(token);
    this.refreshTokenSignal.set(refreshToken);
  }

  private setToken(token: string): void {
    sessionStorage.setItem('auth_token', token);
    this.tokenSubject.next(token);
    this.token.set(token);
  }

  private setUser(user: ProfileResponse): void {
    this.userSubject.next(user);
    this.user.set(user);
  }

  private getStoredToken(): string | null {
    return sessionStorage.getItem('auth_token');
  }

  private getStoredRefreshToken(): string | null {
    const encryptedToken = localStorage.getItem('refresh_token');
    return encryptedToken ? this.decryptToken(encryptedToken) : null;
  }

  clearAuthData(): void {
    sessionStorage.removeItem('auth_token');
    localStorage.removeItem('refresh_token');
    
    this.tokenSubject.next(null);
    this.refreshTokenSubject.next(null);
    this.userSubject.next(null);
    
    this.token.set(null);
    this.refreshTokenSignal.set(null);
    this.user.set(null);
  }

  // Cifrado básico para refresh token (no es criptografía fuerte, pero mejor que texto plano)
  private encryptToken(token: string): string {
    // Cifrado simple con Base64 y XOR (para desarrollo)
    // En producción, usar una librería de criptografía real
    const key = 'SIGRET_SECRET_KEY_2025';
    let encrypted = '';
    
    for (let i = 0; i < token.length; i++) {
      const tokenChar = token.charCodeAt(i);
      const keyChar = key.charCodeAt(i % key.length);
      encrypted += String.fromCharCode(tokenChar ^ keyChar);
    }
    
    return btoa(encrypted);
  }

  private decryptToken(encryptedToken: string): string {
    try {
      const encrypted = atob(encryptedToken);
      const key = 'SIGRET_SECRET_KEY_2025';
      let decrypted = '';
      
      for (let i = 0; i < encrypted.length; i++) {
        const encryptedChar = encrypted.charCodeAt(i);
        const keyChar = key.charCodeAt(i % key.length);
        decrypted += String.fromCharCode(encryptedChar ^ keyChar);
      }
      
      return decrypted;
    } catch (error) {
      console.error('Error decrypting token:', error);
      return '';
    }
  }

  private handleRefreshTokenError(): void {
    this.clearAuthData();
    // No mostrar toast aquí, lo maneja el interceptor
    // Solo redirigir al login
    const router = inject(Router);
    router.navigate(['/login']);
  }

  // Método para obtener el token actual (para interceptors HTTP)
  getToken(): string | null {
    return this.tokenSubject.value;
  }

  // Método para obtener el usuario actual
  getCurrentUser(): ProfileResponse | null {
    return this.userSubject.value;
  }

  // Método para obtener el refresh token actual (para interceptors HTTP)
  getRefreshToken(): string | null {
    return this.refreshTokenSubject.value;
  }
}
