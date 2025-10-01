import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, catchError, throwError } from 'rxjs';
import { 
  User, 
  UserCreateRequest, 
  UserUpdateRequest, 
  UserListResponse, 
  UsernameAvailabilityResponse,
  UserRole 
} from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/usuarios`;
  
  private usersSubject = new BehaviorSubject<User[]>([]);
  public users$ = this.usersSubject.asObservable();

  constructor() {}

  /**
   * Get paginated list of users
   */
  getUsers(page: number = 0, size: number = 10): Observable<UserListResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<UserListResponse>(this.apiUrl, { params }).pipe(
      tap(response => this.usersSubject.next(response.content)),
      catchError(this.handleError)
    );
  }

  /**
   * Get all active users (non-paginated)
   */
  getActiveUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/activos`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get user by ID
   */
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get user by username
   */
  getUserByUsername(username: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/username/${username}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Search users by username
   */
  searchUsersByUsername(username: string): Observable<User[]> {
    const params = new HttpParams().set('username', username);
    return this.http.get<User[]>(`${this.apiUrl}/buscar`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Check username availability
   */
  checkUsernameAvailability(username: string): Observable<boolean> {
    const params = new HttpParams().set('username', username);
    return this.http.get<boolean>(`${this.apiUrl}/verificar-username`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Create new user
   */
  createUser(userData: UserCreateRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, userData).pipe(
      tap(() => this.refreshUsers()),
      catchError(this.handleError)
    );
  }

  /**
   * Update user
   */
  updateUser(id: number, userData: UserUpdateRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, userData).pipe(
      tap(() => this.refreshUsers()),
      catchError(this.handleError)
    );
  }

  /**
   * Activate user
   */
  activateUser(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/activar`, {}).pipe(
      tap(() => this.refreshUsers()),
      catchError(this.handleError)
    );
  }

  /**
   * Deactivate user
   */
  deactivateUser(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/desactivar`, {}).pipe(
      tap(() => this.refreshUsers()),
      catchError(this.handleError)
    );
  }

  /**
   * Change user password
   */
  changePassword(id: number, newPassword: string): Observable<void> {
    const params = new HttpParams().set('nuevaPassword', newPassword);
    return this.http.patch<void>(`${this.apiUrl}/${id}/cambiar-password`, {}, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Delete user permanently
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.refreshUsers()),
      catchError(this.handleError)
    );
  }

  /**
   * Generate automatic username from employee name
   */
  generateUsername(firstName: string, lastName: string): string {
    const first = firstName.toLowerCase().replace(/[^a-z]/g, '');
    const last = lastName.toLowerCase().replace(/[^a-z]/g, '');
    
    if (!first && !last) {
      return 'user' + Math.random().toString(36).substr(2, 4);
    }
    
    if (!first) return last;
    if (!last) return first;
    
    return first + '.' + last;
  }

  /**
   * Get user role display name
   */
  getRoleDisplayName(role: UserRole): string {
    const roleNames: Record<UserRole, string> = {
      [UserRole.PROPIETARIO]: 'Propietario',
      [UserRole.ADMINISTRATIVO]: 'Administrativo',
      [UserRole.TECNICO]: 'Técnico'
    };
    return roleNames[role] || role;
  }

  /**
   * Get user role color for UI
   */
  getRoleColor(role: UserRole): string {
    const roleColors: Record<UserRole, string> = {
      [UserRole.PROPIETARIO]: 'danger',
      [UserRole.ADMINISTRATIVO]: 'warning',
      [UserRole.TECNICO]: 'info'
    };
    return roleColors[role] || 'secondary';
  }

  /**
   * Refresh users list
   */
  private refreshUsers(): void {
    this.getUsers().subscribe();
  }

  /**
   * Handle HTTP errors
   */
  private handleError = (error: any): Observable<never> => {
    console.error('UserService Error:', error);
    
    let errorMessage = 'Ocurrió un error inesperado';
    
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    return throwError(() => new Error(errorMessage));
  };
}
