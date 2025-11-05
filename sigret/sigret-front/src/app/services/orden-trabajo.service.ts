import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrdenTrabajo, EstadoOrdenTrabajo } from '../models/orden-trabajo.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OrdenTrabajoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/ordenes-trabajo`;

  obtenerOrdenesTrabajo(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  obtenerOrdenTrabajoPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  obtenerOrdenesTrabajosPorEstado(estado: EstadoOrdenTrabajo): Observable<OrdenTrabajo[]> {
    return this.http.get<OrdenTrabajo[]>(`${this.apiUrl}/estado/${estado}`);
  }

  obtenerOrdenesTrabajosPorEmpleado(empleadoId: number): Observable<OrdenTrabajo[]> {
    return this.http.get<OrdenTrabajo[]>(`${this.apiUrl}/empleado/${empleadoId}`);
  }

  obtenerOrdenesTrabajosPorServicio(servicioId: number): Observable<OrdenTrabajo[]> {
    return this.http.get<OrdenTrabajo[]>(`${this.apiUrl}/servicio/${servicioId}`);
  }

  crearOrdenTrabajo(ordenTrabajo: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, ordenTrabajo);
  }

  actualizarOrdenTrabajo(id: number, ordenTrabajo: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, ordenTrabajo);
  }

  asignarEmpleado(ordenTrabajoId: number, empleadoId: number): Observable<any> {
    const params = new HttpParams().set('empleadoId', empleadoId.toString());
    return this.http.patch<any>(`${this.apiUrl}/${ordenTrabajoId}/asignar-empleado`, null, { params });
  }

  iniciarOrdenTrabajo(id: number): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/iniciar`, null);
  }

  finalizarOrdenTrabajo(id: number): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/finalizar`, null);
  }

  cambiarEstadoOrdenTrabajo(id: number, nuevoEstado: EstadoOrdenTrabajo): Observable<any> {
    const params = new HttpParams().set('nuevoEstado', nuevoEstado);
    return this.http.patch<any>(`${this.apiUrl}/${id}/cambiar-estado`, null, { params });
  }

  eliminarOrdenTrabajo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
