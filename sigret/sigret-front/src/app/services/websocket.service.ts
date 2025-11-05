import { Injectable, inject, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { ServicioEvent } from '../models/servicio.model';
import { PresupuestoEvent } from '../models/presupuesto.model';
import { OrdenTrabajoEvent } from '../models/orden-trabajo.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client | null = null;

  // Subjects para cada tipo de evento
  private servicioEventSubject = new BehaviorSubject<ServicioEvent | null>(null);
  private presupuestoEventSubject = new BehaviorSubject<PresupuestoEvent | null>(null);
  private ordenTrabajoEventSubject = new BehaviorSubject<OrdenTrabajoEvent | null>(null);

  // Observables públicos
  public servicioEvent$: Observable<ServicioEvent | null> = this.servicioEventSubject.asObservable();
  public presupuestoEvent$: Observable<PresupuestoEvent | null> = this.presupuestoEventSubject.asObservable();
  public ordenTrabajoEvent$: Observable<OrdenTrabajoEvent | null> = this.ordenTrabajoEventSubject.asObservable();

  public isConnected = signal<boolean>(false);

  private readonly authService = inject(AuthService);

  constructor() {}

  private initializeClient(): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-servicios'),
      debug: (str) => { console.log('STOMP Debug:', str); },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      connectHeaders: this.getConnectHeaders(),
    });

    this.client.onConnect = (frame) => {
      console.log('✅ Connected to WebSocket:', frame);
      this.isConnected.set(true);

      // Suscribirse a servicios
      this.client?.subscribe('/topic/servicios', (message: IMessage) => {
        try {
          const event: ServicioEvent = JSON.parse(message.body);
          console.log('📨 Evento Servicio recibido:', event);
          this.servicioEventSubject.next(event);
        } catch (error) {
          console.error('Error parsing Servicio WebSocket message:', error);
        }
      });

      // Suscribirse a presupuestos
      this.client?.subscribe('/topic/presupuestos', (message: IMessage) => {
        try {
          const event: PresupuestoEvent = JSON.parse(message.body);
          console.log('📨 Evento Presupuesto recibido:', event);
          this.presupuestoEventSubject.next(event);
        } catch (error) {
          console.error('Error parsing Presupuesto WebSocket message:', error);
        }
      });

      // Suscribirse a órdenes de trabajo
      this.client?.subscribe('/topic/ordenes-trabajo', (message: IMessage) => {
        try {
          const event: OrdenTrabajoEvent = JSON.parse(message.body);
          console.log('📨 Evento OrdenTrabajo recibido:', event);
          this.ordenTrabajoEventSubject.next(event);
        } catch (error) {
          console.error('Error parsing OrdenTrabajo WebSocket message:', error);
        }
      });
    };

    this.client.onStompError = (frame) => {
      console.error('❌ STOMP error:', frame.headers['message']);
      console.error('Details:', frame.body);
      this.isConnected.set(false);
    };

    this.client.onWebSocketClose = () => {
      console.log('🔌 WebSocket connection closed');
      this.isConnected.set(false);
    };
  }

  private getJwtToken(): string | null {
    return this.authService.getToken();
  }

  private getConnectHeaders(): any {
    const token = this.getJwtToken();
    if (token) {
      return { 'Authorization': `Bearer ${token}` };
    }
    console.warn('⚠️ No JWT token found for WebSocket');
    return {};
  }

  connect(): void {
    if (!this.isConnected() && (!this.client || !this.client.active)) {
      this.initializeClient();
      if (this.client) {
        this.client.connectHeaders = this.getConnectHeaders();
        this.client.activate();
      }
    }
  }

  disconnect(): void {
    if (this.client && this.isConnected()) {
      this.client.deactivate();
      this.isConnected.set(false);
      console.log('🔌 WebSocket disconnected');
    }
  }

  reconnect(): void {
    console.log('🔄 Reconnecting WebSocket with new token...');
    this.disconnect();
    setTimeout(() => { this.connect(); }, 500);
  }

  clearLastEvent(): void {
    this.servicioEventSubject.next(null);
  }

  clearLastPresupuestoEvent(): void {
    this.presupuestoEventSubject.next(null);
  }

  clearLastOrdenTrabajoEvent(): void {
    this.ordenTrabajoEventSubject.next(null);
  }
}
