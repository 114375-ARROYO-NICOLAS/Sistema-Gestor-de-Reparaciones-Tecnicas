import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Presupuesto, EstadoPresupuesto, EnvioPresupuestoDto } from '../models/presupuesto.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PresupuestoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/presupuestos`;

  obtenerPresupuestos(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  obtenerPresupuestoPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  obtenerPresupuestosPorEstado(estado: EstadoPresupuesto): Observable<Presupuesto[]> {
    return this.http.get<Presupuesto[]>(`${this.apiUrl}/estado/${estado}`);
  }

  obtenerPresupuestosPorServicio(servicioId: number): Observable<Presupuesto[]> {
    return this.http.get<Presupuesto[]>(`${this.apiUrl}/servicio/${servicioId}`);
  }

  crearPresupuesto(presupuesto: any): Observable<Presupuesto> {
    return this.http.post<Presupuesto>(this.apiUrl, presupuesto);
  }

  actualizarPresupuesto(id: number, presupuesto: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, presupuesto);
  }

  asignarEmpleado(presupuestoId: number, empleadoId: number): Observable<any> {
    const params = new HttpParams().set('empleadoId', empleadoId.toString());
    return this.http.patch<any>(`${this.apiUrl}/${presupuestoId}/asignar-empleado`, null, { params });
  }

  aprobarPresupuesto(id: number): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/aprobar`, null);
  }

  rechazarPresupuesto(id: number): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${id}/rechazar`, null);
  }

  cambiarEstadoPresupuesto(id: number, nuevoEstado: EstadoPresupuesto): Observable<any> {
    const params = new HttpParams().set('nuevoEstado', nuevoEstado);
    return this.http.patch<any>(`${this.apiUrl}/${id}/cambiar-estado`, null, { params });
  }

  cambiarEstado(id: number, nuevoEstado: EstadoPresupuesto): Observable<any> {
    return this.cambiarEstadoPresupuesto(id, nuevoEstado);
  }

  eliminarPresupuesto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  enviarPresupuestoACliente(dto: EnvioPresupuestoDto): Observable<Presupuesto> {
    return this.http.post<Presupuesto>(`${this.apiUrl}/${dto.presupuestoId}/enviar`, dto);
  }

  crearOrdenDeTrabajo(presupuestoId: number): Observable<{ message: string; ordenTrabajoId: number }> {
    return this.http.post<{ message: string; ordenTrabajoId: number }>(
      `${this.apiUrl}/${presupuestoId}/crear-orden-trabajo`,
      null
    );
  }
}
