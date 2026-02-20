import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PresupuestoPublico } from '../models/presupuesto.model';

@Injectable({
  providedIn: 'root'
})
export class PresupuestoPublicoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/public/presupuestos`;

  obtenerPorToken(token: string): Observable<PresupuestoPublico> {
    return this.http.get<PresupuestoPublico>(`${this.apiUrl}/token/${token}`);
  }

  aprobar(token: string, tipoPrecio?: 'ORIGINAL' | 'ALTERNATIVO'): Observable<{ message: string; numeroPresupuesto: string }> {
    let params = new HttpParams();
    if (tipoPrecio) params = params.set('tipoPrecio', tipoPrecio);
    return this.http.post<{ message: string; numeroPresupuesto: string }>(`${this.apiUrl}/aprobar/${token}`, null, { params });
  }

  rechazar(token: string): Observable<{ message: string; numeroPresupuesto: string }> {
    return this.http.post<{ message: string; numeroPresupuesto: string }>(`${this.apiUrl}/rechazar/${token}`, null);
  }
}
