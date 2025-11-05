import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MarcaListDto, MarcaCreateDto, MarcaUpdateDto, MarcaResponseDto } from '../models/marca.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MarcaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/marcas`;

  /**
   * Get all marcas
   */
  getAllMarcas(): Observable<MarcaListDto[]> {
    return this.http.get<MarcaListDto[]>(`${this.apiUrl}/todos`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get marca by ID
   */
  getMarcaById(id: number): Observable<MarcaResponseDto> {
    return this.http.get<MarcaResponseDto>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Create new marca
   */
  createMarca(marcaData: MarcaCreateDto): Observable<MarcaResponseDto> {
    return this.http.post<MarcaResponseDto>(this.apiUrl, marcaData).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Update marca
   */
  updateMarca(id: number, marcaData: MarcaUpdateDto): Observable<MarcaResponseDto> {
    return this.http.put<MarcaResponseDto>(`${this.apiUrl}/${id}`, marcaData).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Delete marca
   */
  deleteMarca(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handle HTTP errors
   */
  private handleError = (error: any): Observable<never> => {
    console.error('MarcaService Error:', error);
    let errorMessage = 'Ocurrió un error inesperado';
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    return throwError(() => new Error(errorMessage));
  };
}
