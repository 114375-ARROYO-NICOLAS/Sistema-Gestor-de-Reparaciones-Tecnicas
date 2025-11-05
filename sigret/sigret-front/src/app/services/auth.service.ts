import { Injectable, signal, computed, inject, Injector } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, catchError, tap, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { 
  LoginRequest, 
  LoginResponse, 
  RefreshTokenRequest, 
  ProfileResponse, 
  TokenValidationResponse,
  ApiError 
} from '../models/auth.model';
import { WebSocketService } from './websocket.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_BASE_URL = 'http://localhost:8080/auth';
  
  private readonly tokenSubject = new BehaviorSubject<string | null>(this.getStoredToken());
  private readonly refreshTokenSubject = new BehaviorSubject<string | null>(this.getStoredRefreshToken());
  private readonly userSubject = new BehaviorSubject<ProfileResponse | null>(this.getStoredUser());
  
  public readonly token = signal<string | null>(this.tokenSubject.value);
  public readonly refreshTokenSignal = signal<string | null>(this.refreshTokenSubject.value);
  public readonly user = signal<ProfileResponse | null>(this.getStoredUser());
  public readonly isAuthenticated = computed(() => !!this.token());
  public readonly isLoading = signal(false);
  public readonly isRefreshing = signal(false);

  private readonly injector = inject(Injector);

  constructor(private http: HttpClient) {
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
          
          // Lazily inject WebSocketService to break circular dependency
          const webSocketService = this.injector.get(WebSocketService);
          webSocketService.reconnect();
        }),
        catchError(error => {
          this.isRefreshing.set(false);
          this.handleRefreshTokenError();
          return throwError(() => error);
        })
      );
  }

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
          return throwError(() => error);
        })
      );
  }

  validateToken(): Observable<TokenValidationResponse> {
    return this.http.get<TokenValidationResponse>(`${this.API_BASE_URL}/validate`)
      .pipe(
        catchError(error => {
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
          this.clearAuthData();
          return throwError(() => error);
        })
      );
  }

  private setTokens(token: string, refreshToken: string): void {
    sessionStorage.setItem('auth_token', token);
    
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
    localStorage.setItem('user_data', JSON.stringify(user));
    this.userSubject.next(user);
    this.user.set(user);
  }

  private getStoredUser(): ProfileResponse | null {
    const userData = localStorage.getItem('user_data');
    if (userData) {
      try {
        return JSON.parse(userData);
      } catch (error) {
        console.error('Error parsing stored user data:', error);
        return null;
      }
    }
    return null;
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
    localStorage.removeItem('user_data');

    this.tokenSubject.next(null);
    this.refreshTokenSubject.next(null);
    this.userSubject.next(null);

    this.token.set(null);
    this.refreshTokenSignal.set(null);
    this.user.set(null);
  }

  private encryptToken(token: string): string {
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
    const router = inject(Router);
    router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.tokenSubject.value;
  }

  getCurrentUser(): ProfileResponse | null {
    return this.userSubject.value;
  }

  getRefreshToken(): string | null {
    return this.refreshTokenSubject.value;
  }
}
