import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, catchError, throwError } from 'rxjs';
import { 
  Employee, 
  EmployeeCreateRequest, 
  EmployeeUpdateRequest, 
  EmployeeListResponse,
  EmployeeWithoutUser,
  EmployeeType,
  PersonType,
  DocumentType,
  EmployeeResponse,
  EmployeeListDto,
  EmployeeFilterParams
} from '../models/employee.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/empleados`;

  constructor() {}

  /**
   * Get paginated list of employees with filters
   * @param filters - Filtros de búsqueda (activo, busqueda, page, size, sort)
   */
  getEmployees(filters?: EmployeeFilterParams): Observable<EmployeeListResponse> {
    let params = new HttpParams()
      .set('page', (filters?.page ?? 0).toString())
      .set('size', (filters?.size ?? 10).toString())
      .set('sort', filters?.sort ?? 'id,DESC');

    // Solo agregar filtros si tienen valor
    if (filters?.activo !== undefined && filters?.activo !== null) {
      params = params.set('activo', filters.activo.toString());
    }
    
    if (filters?.busqueda) {
      params = params.set('busqueda', filters.busqueda);
    }

    return this.http.get<EmployeeListResponse>(this.apiUrl, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get all active employees (non-paginated)
   */
  getActiveEmployees(): Observable<EmployeeListDto[]> {
    return this.http.get<EmployeeListDto[]>(`${this.apiUrl}/activos`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get employee by ID with complete information
   */
  getEmployeeById(id: number): Observable<EmployeeResponse> {
    return this.http.get<EmployeeResponse>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Create new employee (automatically creates user)
   * Returns complete employee and user information including credentials
   */
  createEmployee(employeeData: EmployeeCreateRequest): Observable<EmployeeResponse> {
    return this.http.post<EmployeeResponse>(this.apiUrl, employeeData).pipe(
      tap(() => this.refreshEmployees()),
      catchError(this.handleError)
    );
  }

  /**
   * Update employee information
   */
  updateEmployee(id: number, employeeData: EmployeeUpdateRequest): Observable<EmployeeResponse> {
    return this.http.put<EmployeeResponse>(`${this.apiUrl}/${id}`, employeeData).pipe(
      tap(() => this.refreshEmployees()),
      catchError(this.handleError)
    );
  }

  /**
   * Activate employee and its associated user
   */
  activateEmployee(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/activar`, {}).pipe(
      tap(() => this.refreshEmployees()),
      catchError(this.handleError)
    );
  }

  /**
   * Deactivate employee and its associated user (logical delete)
   */
  deactivateEmployee(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/desactivar`, {}).pipe(
      tap(() => this.refreshEmployees()),
      catchError(this.handleError)
    );
  }

  /**
   * Permanently delete employee (physical delete)
   */
  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.refreshEmployees()),
      catchError(this.handleError)
    );
  }

  /**
   * Get employee types
   */
  getEmployeeTypes(): Observable<EmployeeType[]> {
    return this.http.get<EmployeeType[]>(`${environment.apiUrl}/tipos-empleado`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get person types
   */
  getPersonTypes(): Observable<PersonType[]> {
    return this.http.get<PersonType[]>(`${environment.apiUrl}/tipos-persona`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get document types
   */
  getDocumentTypes(): Observable<DocumentType[]> {
    return this.http.get<DocumentType[]>(`${environment.apiUrl}/tipos-documento`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get employee status display name
   */
  getStatusDisplayName(activo: boolean): string {
    return activo ? 'Activo' : 'Inactivo';
  }

  /**
   * Get employee status color for UI
   */
  getStatusColor(activo: boolean): string {
    return activo ? 'success' : 'danger';
  }

  /**
   * Format employee full name
   */
  formatEmployeeName(nombreCompleto: string): string {
    return nombreCompleto || 'Sin nombre';
  }

  /**
   * Get available user roles
   */
  getUserRoles(): { label: string; value: string }[] {
    return [
      { label: 'Propietario', value: 'PROPIETARIO' },
      { label: 'Administrativo', value: 'ADMINISTRATIVO' },
      { label: 'Técnico', value: 'TECNICO' }
    ];
  }

  /**
   * Refresh employees list
   */
  private refreshEmployees(): void {
    this.getEmployees().subscribe();
  }

  /**
   * Handle HTTP errors
   */
  private handleError = (error: any): Observable<never> => {
    console.error('EmployeeService Error:', error);
    
    let errorMessage = 'Ocurrió un error inesperado';
    
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    return throwError(() => new Error(errorMessage));
  };
}
