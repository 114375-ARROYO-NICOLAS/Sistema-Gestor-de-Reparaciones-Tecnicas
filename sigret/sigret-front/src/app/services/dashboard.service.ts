import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { DashboardEstadisticas } from '../models/dashboard.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/dashboard`;

  obtenerEstadisticas(fechaDesde?: string, fechaHasta?: string): Observable<DashboardEstadisticas> {
    let params = new HttpParams();
    if (fechaDesde) params = params.set('fechaDesde', fechaDesde);
    if (fechaHasta) params = params.set('fechaHasta', fechaHasta);

    return this.http.get<DashboardEstadisticas>(`${this.apiUrl}/estadisticas`, { params }).pipe(
      catchError((error) => {
        console.error('DashboardService Error:', error);
        return throwError(() => new Error('Error al obtener estadísticas del dashboard'));
      })
    );
  }
}
