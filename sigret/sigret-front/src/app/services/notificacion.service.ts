import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Notificacion, TipoReferencia } from '../models/notificacion.model';
import { WebSocketService } from './websocket.service';

@Injectable({
  providedIn: 'root'
})
export class NotificacionService {
  private readonly API_URL = 'http://localhost:8080/api/notificaciones';
  private readonly http = inject(HttpClient);
  private readonly wsService = inject(WebSocketService);

  readonly notificaciones = signal<Notificacion[]>([]);
  readonly unreadCount = signal<number>(0);
  readonly isLoading = signal<boolean>(false);

  constructor() {
    this.wsService.notificacionEvent$.subscribe(hasNew => {
      if (hasNew) {
        this.loadUnreadCount();
      }
    });
  }

  loadRecientes(): Observable<Notificacion[]> {
    this.isLoading.set(true);
    return this.http.get<Notificacion[]>(this.API_URL).pipe(
      tap(notifs => {
        this.notificaciones.set(notifs);
        this.isLoading.set(false);
      })
    );
  }

  loadUnreadCount(): void {
    this.http.get<number>(`${this.API_URL}/no-leidas/count`)
      .subscribe(count => this.unreadCount.set(count));
  }

  markAsRead(id: number): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/${id}/leer`, {}).pipe(
      tap(() => {
        this.notificaciones.update(notifs =>
          notifs.map(n => n.id === id ? { ...n, leida: true } : n)
        );
        this.unreadCount.update(count => Math.max(0, count - 1));
      })
    );
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/leer-todas`, {}).pipe(
      tap(() => {
        this.notificaciones.update(notifs =>
          notifs.map(n => ({ ...n, leida: true }))
        );
        this.unreadCount.set(0);
      })
    );
  }

  getRouteForNotification(notif: Notificacion): string | null {
    if (!notif.referenciaId || !notif.tipoReferencia) return null;

    const routes: Record<TipoReferencia, string> = {
      'SERVICIO': `/servicios/${notif.referenciaId}`,
      'PRESUPUESTO': `/presupuestos`,
      'ORDEN_TRABAJO': `/ordenes-trabajo`
    };

    return routes[notif.tipoReferencia] ?? null;
  }
}
