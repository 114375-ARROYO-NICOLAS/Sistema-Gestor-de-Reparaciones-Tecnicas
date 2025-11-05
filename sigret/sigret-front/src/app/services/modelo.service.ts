import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ModeloListDto, ModeloCreateDto, ModeloUpdateDto, ModeloResponseDto } from '../models/modelo.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ModeloService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/modelos`;

  /**
   * Get all modelos
   */
  getAllModelos(): Observable<ModeloListDto[]> {
    return this.http.get<ModeloListDto[]>(`${this.apiUrl}/todos`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get modelos by marca
   */
  getModelosByMarca(marcaId: number): Observable<ModeloListDto[]> {
    return this.http.get<ModeloListDto[]>(`${this.apiUrl}/marca/${marcaId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get modelo by ID
   */
  getModeloById(id: number): Observable<ModeloResponseDto> {
    return this.http.get<ModeloResponseDto>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Create new modelo
   */
  createModelo(modeloData: ModeloCreateDto): Observable<ModeloResponseDto> {
    return this.http.post<ModeloResponseDto>(this.apiUrl, modeloData).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Update modelo
   */
  updateModelo(id: number, modeloData: ModeloUpdateDto): Observable<ModeloResponseDto> {
    return this.http.put<ModeloResponseDto>(`${this.apiUrl}/${id}`, modeloData).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Delete modelo
   */
  deleteModelo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handle HTTP errors
   */
  private handleError = (error: any): Observable<never> => {
    console.error('ModeloService Error:', error);
    let errorMessage = 'Ocurrió un error inesperado';
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    return throwError(() => new Error(errorMessage));
  };
}
