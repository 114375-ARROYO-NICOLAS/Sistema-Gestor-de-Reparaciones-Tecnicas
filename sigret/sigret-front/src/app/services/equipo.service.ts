import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  Equipo,
  EquipoListDto,
  EquipoCreateDto,
  EquipoUpdateDto,
  EquipoResponseDto,
  EquipoListResponse,
  EquipoFilterParams
} from '../models/equipo.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EquipoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/equipos`;

  /**
   * Get paginated list of equipos
   */
  getEquipos(filters?: EquipoFilterParams): Observable<EquipoListResponse> {
    let params = new HttpParams()
      .set('page', (filters?.page ?? 0).toString())
      .set('size', (filters?.size ?? 10).toString())
      .set('sort', filters?.sort ?? 'id,DESC');

    return this.http.get<EquipoListResponse>(this.apiUrl, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get all equipos (no pagination)
   */
  getAllEquipos(): Observable<EquipoListDto[]> {
    return this.http.get<EquipoListDto[]>(`${this.apiUrl}/todos`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get equipos by cliente ID
   */
  getEquiposByCliente(clienteId: number): Observable<EquipoListDto[]> {
    return this.http.get<EquipoListDto[]>(`${this.apiUrl}/cliente/${clienteId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Search equipos by term
   */
  searchEquipos(termino: string): Observable<EquipoListDto[]> {
    const params = new HttpParams().set('termino', termino);
    return this.http.get<EquipoListDto[]>(`${this.apiUrl}/buscar`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get equipo by ID
   */
  getEquipoById(id: number): Observable<EquipoResponseDto> {
    return this.http.get<EquipoResponseDto>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Create new equipo
   */
  createEquipo(equipoData: EquipoCreateDto): Observable<EquipoResponseDto> {
    return this.http.post<EquipoResponseDto>(this.apiUrl, equipoData).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Update equipo
   */
  updateEquipo(id: number, equipoData: EquipoUpdateDto): Observable<EquipoResponseDto> {
    return this.http.put<EquipoResponseDto>(`${this.apiUrl}/${id}`, equipoData).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Delete equipo
   */
  deleteEquipo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Verify if numero de serie exists
   */
  verificarNumeroSerie(numeroSerie: string): Observable<boolean> {
    const params = new HttpParams().set('numeroSerie', numeroSerie);
    return this.http.get<boolean>(`${this.apiUrl}/verificar-numero-serie`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get equipos by marca
   */
  getEquiposByMarca(marcaId: number): Observable<EquipoListDto[]> {
    return this.http.get<EquipoListDto[]>(`${this.apiUrl}/marca/${marcaId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get equipos by tipo
   */
  getEquiposByTipo(tipoEquipoId: number): Observable<EquipoListDto[]> {
    return this.http.get<EquipoListDto[]>(`${this.apiUrl}/tipo/${tipoEquipoId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Associate equipo to cliente
   */
  asociarEquipoACliente(equipoId: number, clienteId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${equipoId}/cliente/${clienteId}`, {}).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Disassociate equipo from cliente
   */
  desasociarEquipoDeCliente(equipoId: number, clienteId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${equipoId}/cliente/${clienteId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Format equipo display name
   */
  getEquipoDisplay(equipo: EquipoListDto): string {
    const parts = [equipo.descripcionCompleta];
    if (equipo.numeroSerie) {
      parts.push(`S/N: ${equipo.numeroSerie}`);
    }
    return parts.join(' - ');
  }

  /**
   * Handle HTTP errors
   */
  private handleError = (error: any): Observable<never> => {
    console.error('EquipoService Error:', error);

    let errorMessage = 'Ocurrió un error inesperado';

    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }

    return throwError(() => new Error(errorMessage));
  };
}
