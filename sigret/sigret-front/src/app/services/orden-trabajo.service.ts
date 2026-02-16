import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrdenTrabajo, EstadoOrdenTrabajo } from '../models/orden-trabajo.model';
import { ItemEvaluacionGarantia } from '../models/item-evaluacion-garantia.model';
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

  /**
   * Crea una orden de trabajo sin costo para garantía que cumple condiciones
   */
  crearOrdenTrabajoGarantia(servicioId: number, empleadoId: number, observaciones?: string, itemsEvaluacion?: ItemEvaluacionGarantia[]): Observable<any> {
    let params = new HttpParams()
      .set('servicioId', servicioId.toString())
      .set('empleadoId', empleadoId.toString());

    if (observaciones) {
      params = params.set('observaciones', observaciones);
    }

    return this.http.post<any>(`${this.apiUrl}/garantia`, itemsEvaluacion || null, { params });
  }

  actualizarDetalleOrdenTrabajo(ordenTrabajoId: number, detalleId: number, comentario?: string, completado?: boolean): Observable<any> {
    let params = new HttpParams();
    if (comentario !== undefined && comentario !== null) {
      params = params.set('comentario', comentario);
    }
    if (completado !== undefined && completado !== null) {
      params = params.set('completado', completado.toString());
    }
    return this.http.patch<any>(`${this.apiUrl}/${ordenTrabajoId}/detalles/${detalleId}`, null, { params });
  }

  verificarDetallesCompletados(ordenTrabajoId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${ordenTrabajoId}/detalles-completados`);
  }
}
