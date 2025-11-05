import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TipoEquipoListDto, TipoEquipoCreateDto, TipoEquipoUpdateDto, TipoEquipoResponseDto } from '../models/tipo-equipo.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TipoEquipoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/tipos-equipo`;

  /**
   * Get all tipos de equipo
   */
  getAllTiposEquipo(): Observable<TipoEquipoListDto[]> {
    return this.http.get<TipoEquipoListDto[]>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get tipo equipo by ID
   */
  getTipoEquipoById(id: number): Observable<TipoEquipoResponseDto> {
    return this.http.get<TipoEquipoResponseDto>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Create new tipo equipo
   */
  createTipoEquipo(tipoEquipoData: TipoEquipoCreateDto): Observable<TipoEquipoResponseDto> {
    return this.http.post<TipoEquipoResponseDto>(this.apiUrl, tipoEquipoData).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Update tipo equipo
   */
  updateTipoEquipo(id: number, tipoEquipoData: TipoEquipoUpdateDto): Observable<TipoEquipoResponseDto> {
    return this.http.put<TipoEquipoResponseDto>(`${this.apiUrl}/${id}`, tipoEquipoData).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Delete tipo equipo
   */
  deleteTipoEquipo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handle HTTP errors
   */
  private handleError = (error: any): Observable<never> => {
    console.error('TipoEquipoService Error:', error);
    let errorMessage = 'Ocurrió un error inesperado';
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    return throwError(() => new Error(errorMessage));
  };
}
