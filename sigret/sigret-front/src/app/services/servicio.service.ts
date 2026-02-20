import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import {
  ServicioList,
  ServicioResponse,
  ServicioCreateDto,
  EstadoServicio,
  PaginatedResponse
} from '../models/servicio.model';
import { ServicioUpdateDto } from '../models/servicio-update.dto';
import { ItemServicioOriginal } from '../models/item-evaluacion-garantia.model';

@Injectable({
  providedIn: 'root'
})
export class ServicioService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/servicios';

  /**
   * Obtiene todos los servicios sin paginación
   */
  obtenerTodosLosServicios(): Observable<ServicioList[]> {
    const params = new HttpParams()
      .set('size', '1000')
      .set('sort', 'fechaCreacion,desc');

    return this.http.get<PaginatedResponse<ServicioList>>(this.API_URL, { params }).pipe(
      map(response => response.content || [])
    );
  }

  /**
   * Obtiene servicios paginados
   */
  obtenerServiciosPaginados(page: number = 0, size: number = 20): Observable<PaginatedResponse<ServicioList>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', 'fechaCreacion,desc');

    return this.http.get<PaginatedResponse<ServicioList>>(this.API_URL, { params });
  }

  /**
   * Obtiene los estados disponibles desde el backend
   */
  obtenerEstados(): Observable<{ value: string; label: string }[]> {
    return this.http.get<{ value: string; label: string }[]>(`${this.API_URL}/estados`);
  }

  /**
   * Obtiene servicios por estado
   */
  obtenerServiciosPorEstado(estado: EstadoServicio): Observable<ServicioList[]> {
    return this.http.get<ServicioList[]>(`${this.API_URL}/estado/${estado}`);
  }

  /**
   * Obtiene un servicio por ID
   */
  obtenerServicioPorId(id: number): Observable<ServicioResponse> {
    return this.http.get<ServicioResponse>(`${this.API_URL}/${id}`);
  }

  /**
   * Crea un nuevo servicio
   */
  crearServicio(servicio: ServicioCreateDto): Observable<ServicioResponse> {
    return this.http.post<ServicioResponse>(this.API_URL, servicio);
  }

  /**
   * Actualiza un servicio existente
   */
  actualizarServicio(id: number, servicio: ServicioUpdateDto): Observable<ServicioResponse> {
    return this.http.put<ServicioResponse>(`${this.API_URL}/${id}`, servicio);
  }

  /**
   * Cambia el estado de un servicio
   */
  cambiarEstado(id: number, nuevoEstado: EstadoServicio): Observable<ServicioResponse> {
    const params = new HttpParams().set('nuevoEstado', nuevoEstado);
    return this.http.patch<ServicioResponse>(`${this.API_URL}/${id}/cambiar-estado`, null, { params });
  }

  /**
   * Elimina un servicio (soft delete)
   */
  eliminarServicio(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  /**
   * Obtiene servicios eliminados
   */
  obtenerServiciosEliminados(): Observable<ServicioList[]> {
    const params = new HttpParams()
      .set('size', '1000')
      .set('sort', 'fechaCreacion,desc');

    return this.http.get<PaginatedResponse<ServicioList>>(`${this.API_URL}/eliminados`, { params }).pipe(
      map(response => response.content || [])
    );
  }

  /**
   * Restaura un servicio eliminado
   */
  restaurarServicio(id: number): Observable<ServicioResponse> {
    return this.http.patch<ServicioResponse>(`${this.API_URL}/${id}/restaurar`, null);
  }

  /**
   * Busca servicios por número de servicio
   */
  buscarPorNumeroServicio(numeroServicio: string): Observable<ServicioList[]> {
    return this.http.get<ServicioList[]>(`${this.API_URL}/numero/${numeroServicio}`);
  }

  /**
   * Filtra servicios por cliente
   */
  filtrarPorCliente(clienteId: number): Observable<ServicioList[]> {
    return this.http.get<ServicioList[]>(`${this.API_URL}/cliente/${clienteId}`);
  }

  /**
   * Obtiene servicios de un cliente específico
   */
  obtenerServiciosPorCliente(clienteId: number): Observable<ServicioList[]> {
    return this.http.get<ServicioList[]>(`${this.API_URL}/cliente/${clienteId}`);
  }

  /**
   * Obtiene servicios elegibles para crear garantía (TERMINADOS y dentro de plazo)
   */
  obtenerServiciosElegiblesParaGarantia(clienteId: number): Observable<ServicioList[]> {
    return this.obtenerServiciosPorCliente(clienteId).pipe(
      map(servicios => servicios.filter(s =>
        s.estado === EstadoServicio.TERMINADO &&
        s.fechaDevolucionReal &&
        this.estaDentroDePlazoGarantia(s.fechaDevolucionReal)
      ))
    );
  }

  /**
   * Verifica si un servicio está dentro del plazo de garantía (90 días)
   */
  private estaDentroDePlazoGarantia(fechaDevolucionReal: string): boolean {
    const fechaDevolucion = new Date(fechaDevolucionReal);
    const fechaLimite = new Date();
    fechaLimite.setDate(fechaLimite.getDate() - 90);
    return fechaDevolucion >= fechaLimite;
  }

  /**
   * Filtra servicios por rango de fechas
   */
  filtrarPorFechas(fechaInicio: string, fechaFin: string): Observable<ServicioList[]> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    return this.http.get<ServicioList[]>(`${this.API_URL}/fechas`, { params });
  }

  /**
   * Obtiene todos los servicios de garantía
   */
  obtenerServiciosGarantia(): Observable<ServicioList[]> {
    return this.http.get<ServicioList[]>(`${this.API_URL}/garantias`);
  }

  /**
   * Crea un servicio de garantía basado en un servicio original
   */
  crearServicioGarantia(servicioOriginalId: number, datos: Partial<ServicioCreateDto>): Observable<ServicioResponse> {
    return this.http.post<ServicioResponse>(`${this.API_URL}/garantia/${servicioOriginalId}`, datos);
  }

  /**
   * Obtiene los items (repuestos) usados en la reparación del servicio original
   * Para mostrar en la evaluación de garantía
   */
  obtenerItemsServicioOriginal(servicioGarantiaId: number): Observable<ItemServicioOriginal[]> {
    return this.http.get<ItemServicioOriginal[]>(`${this.API_URL}/${servicioGarantiaId}/items-servicio-original`);
  }

  /**
   * Descarga el PDF del servicio
   */
  descargarPdfServicio(id: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${id}/pdf`, {
      responseType: 'blob'
    });
  }

  /**
   * Envía el PDF por email al cliente
   */
  enviarPdfPorEmail(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${id}/pdf/enviar-email`, null);
  }

  /**
   * Finaliza un servicio registrando la firma de conformidad
   */
  finalizarServicio(id: number, firmaConformidad: string): Observable<ServicioResponse> {
    return this.http.patch<ServicioResponse>(`${this.API_URL}/${id}/finalizar`, { firmaConformidad });
  }

  /**
   * Descarga el PDF final del servicio (con presupuesto, orden de trabajo y firma de conformidad)
   */
  descargarPdfFinal(id: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${id}/pdf-final`, {
      responseType: 'blob'
    });
  }

  /**
   * Envía el PDF final por email al cliente
   */
  enviarPdfFinalPorEmail(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/${id}/pdf-final/enviar-email`, null);
  }
}
