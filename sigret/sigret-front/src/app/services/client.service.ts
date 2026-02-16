import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { 
  Client,
  ClientCreateRequest,
  ClientUpdateRequest,
  ClientListResponse,
  ClientResponse,
  ClientListDto,
  ClientFilterParams,
  ClientAutocompleteParams,
  PersonType,
  DocumentType
} from '../models/client.model';
import { TipoContacto } from '../models/contact.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/clientes`;

  /**
   * Get paginated list of clients with optional filter
   * @param filters - Filter parameters (page, size, sort, filtro)
   */
  getClients(filters?: ClientFilterParams): Observable<ClientListResponse> {
    let params = new HttpParams()
      .set('page', (filters?.page ?? 0).toString())
      .set('size', (filters?.size ?? 10).toString())
      .set('sort', filters?.sort ?? 'id,DESC');

    // Add filter if provided
    if (filters?.filtro && filters.filtro.trim()) {
      params = params.set('filtro', filters.filtro);
    }

    return this.http.get<ClientListResponse>(this.apiUrl, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Autocomplete search for clients
   * @param params - Search term and optional limit
   */
  autocompleteClients(params: ClientAutocompleteParams): Observable<ClientListDto[]> {
    let httpParams = new HttpParams()
      .set('termino', params.termino)
      .set('limite', (params.limite ?? 10).toString());

    return this.http.get<ClientListDto[]>(`${this.apiUrl}/autocompletado`, { params: httpParams }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get client by ID with complete information
   */
  getClientById(id: number): Observable<ClientResponse> {
    return this.http.get<ClientResponse>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Create new client
   */
  createClient(clientData: ClientCreateRequest): Observable<ClientResponse> {
    return this.http.post<ClientResponse>(this.apiUrl, clientData).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Update client information
   */
  updateClient(id: number, clientData: ClientUpdateRequest): Observable<ClientResponse> {
    return this.http.put<ClientResponse>(`${this.apiUrl}/${id}`, clientData).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Deactivate client (logical delete)
   */
  deactivateClient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get paginated list of inactive (deleted) clients
   */
  getInactiveClients(page = 0, size = 10): Observable<ClientListResponse> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'id,DESC');

    return this.http.get<ClientListResponse>(`${this.apiUrl}/inactivos`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Reactivate a deactivated client
   */
  reactivateClient(id: number): Observable<ClientResponse> {
    return this.http.put<ClientResponse>(`${this.apiUrl}/${id}/reactivar`, {}).pipe(
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
   * Get contact types (Email, Teléfono, Celular, WhatsApp, etc.)
   */
  getTiposContacto(): Observable<TipoContacto[]> {
    return this.http.get<TipoContacto[]>(`${environment.apiUrl}/tipos-contacto`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get client status display name
   */
  getStatusDisplayName(activo: boolean): string {
    return activo ? 'Activo' : 'Inactivo';
  }

  /**
   * Get client status color for UI
   */
  getStatusColor(activo: boolean): 'success' | 'danger' {
    return activo ? 'success' : 'danger';
  }

  /**
   * Format client full name
   */
  formatClientName(nombreCompleto: string): string {
    return nombreCompleto || 'Sin nombre';
  }

  /**
   * Handle HTTP errors
   */
  private handleError = (error: any): Observable<never> => {
    console.error('ClientService Error:', error);
    
    let errorMessage = 'Ocurrió un error inesperado';
    
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    
    return throwError(() => new Error(errorMessage));
  };
}

