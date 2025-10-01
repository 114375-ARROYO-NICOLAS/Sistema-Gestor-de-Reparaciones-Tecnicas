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
  DocumentType
} from '../models/employee.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}`;
  
  private employeesSubject = new BehaviorSubject<Employee[]>([]);
  public employees$ = this.employeesSubject.asObservable();

  constructor() {}

  /**
   * Get paginated list of employees
   * Note: This endpoint might need to be implemented in the backend
   */
  getEmployees(page: number = 0, size: number = 10): Observable<EmployeeListResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    // This endpoint needs to be implemented in the backend
    return this.http.get<EmployeeListResponse>(`${this.apiUrl}/empleados`, { params }).pipe(
      tap(response => this.employeesSubject.next(response.content)),
      catchError(this.handleError)
    );
  }

  /**
   * Get all active employees
   */
  getActiveEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiUrl}/empleados/activos`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get employees without user accounts
   */
  getEmployeesWithoutUser(): Observable<EmployeeWithoutUser[]> {
    return this.http.get<EmployeeWithoutUser[]>(`${this.apiUrl}/empleados/sin-usuario`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get employee by ID
   */
  getEmployeeById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/empleados/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Create new employee
   */
  createEmployee(employeeData: EmployeeCreateRequest): Observable<Employee> {
    return this.http.post<Employee>(`${this.apiUrl}/empleados`, employeeData).pipe(
      tap(() => this.refreshEmployees()),
      catchError(this.handleError)
    );
  }

  /**
   * Update employee
   */
  updateEmployee(id: number, employeeData: EmployeeUpdateRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiUrl}/empleados/${id}`, employeeData).pipe(
      tap(() => this.refreshEmployees()),
      catchError(this.handleError)
    );
  }

  /**
   * Activate employee
   */
  activateEmployee(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/empleados/${id}/activar`, {}).pipe(
      tap(() => this.refreshEmployees()),
      catchError(this.handleError)
    );
  }

  /**
   * Deactivate employee
   */
  deactivateEmployee(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/empleados/${id}/desactivar`, {}).pipe(
      tap(() => this.refreshEmployees()),
      catchError(this.handleError)
    );
  }

  /**
   * Delete employee
   */
  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/empleados/${id}`).pipe(
      tap(() => this.refreshEmployees()),
      catchError(this.handleError)
    );
  }

  /**
   * Get employee types
   */
  getEmployeeTypes(): Observable<EmployeeType[]> {
    return this.http.get<EmployeeType[]>(`${this.apiUrl}/tipos-empleado`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get person types
   */
  getPersonTypes(): Observable<PersonType[]> {
    return this.http.get<PersonType[]>(`${this.apiUrl}/tipos-persona`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get document types
   */
  getDocumentTypes(): Observable<DocumentType[]> {
    return this.http.get<DocumentType[]>(`${this.apiUrl}/tipos-documento`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Check if document number is available
   */
  checkDocumentAvailability(document: string): Observable<boolean> {
    const params = new HttpParams().set('documento', document);
    return this.http.get<boolean>(`${this.apiUrl}/empleados/verificar-documento`, { params }).pipe(
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
  formatEmployeeName(employee: Employee): string {
    return employee.nombreCompleto || 'Sin nombre';
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
