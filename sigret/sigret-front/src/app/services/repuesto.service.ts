import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Repuesto } from '../models/repuesto.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RepuestoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/repuestos`;

  buscar(query?: string): Observable<Repuesto[]> {
    let params = new HttpParams();
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Repuesto[]>(`${this.apiUrl}/buscar`, { params });
  }
}
