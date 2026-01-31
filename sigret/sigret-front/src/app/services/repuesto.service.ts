import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Repuesto, RepuestoListDto, RepuestoCreateDto, RepuestoUpdateDto, RepuestoResponseDto } from '../models/repuesto.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RepuestoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/repuestos`;

  buscar(query?: string): Observable<Repuesto[]> {
    let params = new HttpParams();
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Repuesto[]>(`${this.apiUrl}/buscar`, { params });
  }

  getAllRepuestos(): Observable<RepuestoListDto[]> {
    return this.http.get<RepuestoListDto[]>(this.apiUrl).pipe(
      catchError(this.handleError)
    );
  }

  getRepuestoById(id: number): Observable<RepuestoResponseDto> {
    return this.http.get<RepuestoResponseDto>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getRepuestosPorTipoEquipo(tipoEquipoId: number): Observable<RepuestoListDto[]> {
    return this.http.get<RepuestoListDto[]>(`${this.apiUrl}/tipo-equipo/${tipoEquipoId}`).pipe(
      catchError(this.handleError)
    );
  }

  createRepuesto(repuestoData: RepuestoCreateDto): Observable<RepuestoResponseDto> {
    return this.http.post<RepuestoResponseDto>(this.apiUrl, repuestoData).pipe(
      catchError(this.handleError)
    );
  }

  updateRepuesto(id: number, repuestoData: RepuestoUpdateDto): Observable<RepuestoResponseDto> {
    return this.http.put<RepuestoResponseDto>(`${this.apiUrl}/${id}`, repuestoData).pipe(
      catchError(this.handleError)
    );
  }

  deleteRepuesto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError = (error: any): Observable<never> => {
    console.error('RepuestoService Error:', error);
    let errorMessage = 'Ocurrió un error inesperado';
    if (error.error?.message) {
      errorMessage = error.error.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    return throwError(() => new Error(errorMessage));
  };
}
