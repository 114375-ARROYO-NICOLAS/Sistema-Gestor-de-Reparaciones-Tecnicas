# Implementación del Tablero Kanban de Servicios - Frontend Angular

## Resumen
Implementar un tablero Kanban estilo Jira para gestionar servicios técnicos con actualizaciones en tiempo real mediante WebSocket.

## Backend Completado ✅

### 1. Entidades y DTOs
- ✅ Entidad `Servicio` con todos los campos necesarios
- ✅ Entidad `DetalleServicio` para componentes del equipo
- ✅ `ServicioCreateDto` que incluye lista de detalles
- ✅ `ServicioResponseDto` con detalles completos
- ✅ `ServicioListDto` para listados
- ✅ `ServicioEventDto` para eventos WebSocket

### 2. Enumeración de Estados
```java
public enum EstadoServicio {
    RECIBIDO("Recibido"),
    ESPERANDO_EVALUACION_GARANTIA("Esperando Evaluación Garantía"),
    PRESUPUESTADO("Presupuestado"),
    APROBADO("Aprobado"),
    EN_REPARACION("En Reparación"),
    TERMINADO("Terminado"),
    RECHAZADO("Rechazado"),
    GARANTIA_SIN_REPARACION("Garantía Sin Reparación"),
    GARANTIA_RECHAZADA("Garantía Rechazada")
}
```

### 3. Endpoints Disponibles
- ✅ `POST /api/servicios` - Crear servicio (genera número automático)
- ✅ `GET /api/servicios` - Listar servicios (paginado)
- ✅ `GET /api/servicios/{id}` - Obtener servicio por ID
- ✅ `GET /api/servicios/estado/{estado}` - Filtrar por estado
- ✅ `PUT /api/servicios/{id}` - Actualizar servicio
- ✅ `PATCH /api/servicios/{id}/cambiar-estado?nuevoEstado={estado}` - Cambiar estado
- ✅ `DELETE /api/servicios/{id}` - Eliminar servicio

### 4. WebSocket Configurado
- ✅ Endpoint: `ws://localhost:8080/ws-servicios`
- ✅ Topic: `/topic/servicios`
- ✅ Eventos emitidos:
  - `CREADO` - Cuando se crea un servicio
  - `ACTUALIZADO` - Cuando se actualiza un servicio
  - `ESTADO_CAMBIADO` - Cuando cambia el estado (drag & drop)
  - `ELIMINADO` - Cuando se elimina un servicio

---

## Frontend a Implementar en Angular

### 1. Instalar Dependencias

```bash
cd sigret-front
npm install @stomp/stompjs sockjs-client
npm install --save-dev @types/sockjs-client
npm install @angular/cdk @angular/cdk-experimental
```

### 2. Crear Modelos TypeScript

**src/app/models/servicio.model.ts**
```typescript
export enum EstadoServicio {
  RECIBIDO = 'RECIBIDO',
  ESPERANDO_EVALUACION_GARANTIA = 'ESPERANDO_EVALUACION_GARANTIA',
  PRESUPUESTADO = 'PRESUPUESTADO',
  APROBADO = 'APROBADO',
  EN_REPARACION = 'EN_REPARACION',
  TERMINADO = 'TERMINADO',
  RECHAZADO = 'RECHAZADO',
  GARANTIA_SIN_REPARACION = 'GARANTIA_SIN_REPARACION',
  GARANTIA_RECHAZADA = 'GARANTIA_RECHAZADA'
}

export interface DetalleServicio {
  id?: number;
  componente: string;
  presente: boolean;
  comentario?: string;
}

export interface ServicioList {
  id: number;
  numeroServicio: string;
  clienteNombre: string;
  clienteDocumento: string;
  equipoDescripcion: string;
  equipoNumeroSerie: string;
  empleadoRecepcionNombre: string;
  tipoIngreso: string;
  esGarantia: boolean;
  abonaVisita: boolean;
  montoVisita: number;
  montoPagado: number;
  estado: EstadoServicio;
  fechaCreacion: string;
  fechaRecepcion: string;
  fechaDevolucionPrevista?: string;
  fechaDevolucionReal?: string;
}

export interface ServicioEvent {
  tipo: 'CREADO' | 'ACTUALIZADO' | 'ESTADO_CAMBIADO' | 'ELIMINADO';
  servicioId: number;
  numeroServicio: string;
  estadoAnterior?: EstadoServicio;
  estadoNuevo?: EstadoServicio;
  timestamp: string;
  servicio?: ServicioList;
}
```

### 3. Crear Servicio WebSocket

**src/app/services/websocket.service.ts**
```typescript
import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { ServicioEvent } from '../models/servicio.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private servicioEventSubject = new BehaviorSubject<ServicioEvent | null>(null);
  public servicioEvent$: Observable<ServicioEvent | null> = this.servicioEventSubject.asObservable();
  private isConnected = false;

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-servicios'),
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = (frame) => {
      console.log('Connected to WebSocket:', frame);
      this.isConnected = true;

      this.client.subscribe('/topic/servicios', (message: IMessage) => {
        const event: ServicioEvent = JSON.parse(message.body);
        console.log('Evento recibido:', event);
        this.servicioEventSubject.next(event);
      });
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP error:', frame);
      this.isConnected = false;
    };

    this.client.onWebSocketClose = () => {
      console.log('WebSocket connection closed');
      this.isConnected = false;
    };
  }

  connect(): void {
    if (!this.isConnected) {
      this.client.activate();
    }
  }

  disconnect(): void {
    if (this.isConnected) {
      this.client.deactivate();
      this.isConnected = false;
    }
  }

  getConnectionStatus(): boolean {
    return this.isConnected;
  }
}
```

### 4. Crear Servicio de Servicios

**src/app/services/servicio.service.ts**
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ServicioList, EstadoServicio } from '../models/servicio.model';

@Injectable({
  providedIn: 'root'
})
export class ServicioService {
  private apiUrl = 'http://localhost:8080/api/servicios';

  constructor(private http: HttpClient) {}

  obtenerTodosLosServicios(): Observable<ServicioList[]> {
    // Para obtener todos sin paginación, puedes usar size=1000 o crear endpoint específico
    const params = new HttpParams().set('size', '1000');
    return this.http.get<any>(this.apiUrl, { params }).pipe(
      map(response => response.content || [])
    );
  }

  obtenerServiciosPorEstado(estado: EstadoServicio): Observable<ServicioList[]> {
    return this.http.get<ServicioList[]>(`${this.apiUrl}/estado/${estado}`);
  }

  cambiarEstado(id: number, nuevoEstado: EstadoServicio): Observable<any> {
    const params = new HttpParams().set('nuevoEstado', nuevoEstado);
    return this.http.patch(`${this.apiUrl}/${id}/cambiar-estado`, null, { params });
  }

  // Más métodos según necesites...
}
```

### 5. Crear Componente Tablero Kanban

**Generar componente:**
```bash
ng generate component components/tablero-servicios
```

**src/app/components/tablero-servicios/tablero-servicios.component.ts**
```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { Subscription } from 'rxjs';
import { ServicioService } from '../../services/servicio.service';
import { WebSocketService } from '../../services/websocket.service';
import { ServicioList, EstadoServicio, ServicioEvent } from '../../models/servicio.model';

interface Columna {
  nombre: string;
  estado: EstadoServicio;
  color: string;
  servicios: ServicioList[];
}

@Component({
  selector: 'app-tablero-servicios',
  templateUrl: './tablero-servicios.component.html',
  styleUrls: ['./tablero-servicios.component.scss']
})
export class TableroServiciosComponent implements OnInit, OnDestroy {
  columnas: Columna[] = [
    { nombre: 'Recibido', estado: EstadoServicio.RECIBIDO, color: '#6c757d', servicios: [] },
    { nombre: 'Presupuestado', estado: EstadoServicio.PRESUPUESTADO, color: '#ffc107', servicios: [] },
    { nombre: 'Aprobado', estado: EstadoServicio.APROBADO, color: '#17a2b8', servicios: [] },
    { nombre: 'En Reparación', estado: EstadoServicio.EN_REPARACION, color: '#007bff', servicios: [] },
    { nombre: 'Terminado', estado: EstadoServicio.TERMINADO, color: '#28a745', servicios: [] },
    { nombre: 'Rechazado', estado: EstadoServicio.RECHAZADO, color: '#dc3545', servicios: [] }
  ];

  private wsSubscription?: Subscription;
  loading = true;
  error: string | null = null;

  constructor(
    private servicioService: ServicioService,
    private wsService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.cargarServicios();
    this.conectarWebSocket();
  }

  ngOnDestroy(): void {
    this.wsSubscription?.unsubscribe();
    this.wsService.disconnect();
  }

  cargarServicios(): void {
    this.loading = true;
    this.servicioService.obtenerTodosLosServicios().subscribe({
      next: (servicios) => {
        this.organizarServiciosPorEstado(servicios);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error al cargar servicios:', err);
        this.error = 'Error al cargar los servicios';
        this.loading = false;
      }
    });
  }

  organizarServiciosPorEstado(servicios: ServicioList[]): void {
    // Limpiar columnas
    this.columnas.forEach(col => col.servicios = []);

    // Distribuir servicios por estado
    servicios.forEach(servicio => {
      const columna = this.columnas.find(col => col.estado === servicio.estado);
      if (columna) {
        columna.servicios.push(servicio);
      }
    });
  }

  conectarWebSocket(): void {
    this.wsService.connect();

    this.wsSubscription = this.wsService.servicioEvent$.subscribe({
      next: (event: ServicioEvent | null) => {
        if (event) {
          this.manejarEventoWebSocket(event);
        }
      },
      error: (err) => {
        console.error('Error en WebSocket:', err);
      }
    });
  }

  manejarEventoWebSocket(event: ServicioEvent): void {
    switch (event.tipo) {
      case 'CREADO':
        if (event.servicio) {
          this.agregarServicio(event.servicio);
        }
        break;
      case 'ACTUALIZADO':
        if (event.servicio) {
          this.actualizarServicio(event.servicio);
        }
        break;
      case 'ESTADO_CAMBIADO':
        if (event.servicio && event.estadoAnterior) {
          this.moverServicio(event.servicio, event.estadoAnterior);
        }
        break;
      case 'ELIMINADO':
        this.eliminarServicio(event.servicioId);
        break;
    }
  }

  agregarServicio(servicio: ServicioList): void {
    const columna = this.columnas.find(col => col.estado === servicio.estado);
    if (columna) {
      // Verificar que no exista ya
      const existe = columna.servicios.some(s => s.id === servicio.id);
      if (!existe) {
        columna.servicios.unshift(servicio);
      }
    }
  }

  actualizarServicio(servicio: ServicioList): void {
    const columna = this.columnas.find(col => col.estado === servicio.estado);
    if (columna) {
      const index = columna.servicios.findIndex(s => s.id === servicio.id);
      if (index !== -1) {
        columna.servicios[index] = servicio;
      }
    }
  }

  moverServicio(servicio: ServicioList, estadoAnterior: EstadoServicio): void {
    // Eliminar del estado anterior
    const columnaAnterior = this.columnas.find(col => col.estado === estadoAnterior);
    if (columnaAnterior) {
      columnaAnterior.servicios = columnaAnterior.servicios.filter(s => s.id !== servicio.id);
    }

    // Agregar al nuevo estado
    const columnaNueva = this.columnas.find(col => col.estado === servicio.estado);
    if (columnaNueva) {
      columnaNueva.servicios.push(servicio);
    }
  }

  eliminarServicio(servicioId: number): void {
    this.columnas.forEach(columna => {
      columna.servicios = columna.servicios.filter(s => s.id !== servicioId);
    });
  }

  onDrop(event: CdkDragDrop<ServicioList[]>): void {
    if (event.previousContainer === event.container) {
      // Reordenar dentro de la misma columna
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      // Mover entre columnas
      const servicio = event.previousContainer.data[event.previousIndex];
      const columnaDestino = this.columnas.find(col => col.servicios === event.container.data);

      if (columnaDestino && servicio) {
        // Cambiar estado en el backend
        this.servicioService.cambiarEstado(servicio.id, columnaDestino.estado).subscribe({
          next: () => {
            // El WebSocket se encargará de actualizar el tablero
            console.log('Estado cambiado exitosamente');
          },
          error: (err) => {
            console.error('Error al cambiar estado:', err);
            // Revertir cambio visual si falla
            this.cargarServicios();
          }
        });

        // Actualizar visualmente de inmediato (optimistic update)
        transferArrayItem(
          event.previousContainer.data,
          event.container.data,
          event.previousIndex,
          event.currentIndex
        );
      }
    }
  }

  getConectedListIds(): string[] {
    return this.columnas.map((_, index) => `columna-${index}`);
  }
}
```

**src/app/components/tablero-servicios/tablero-servicios.component.html**
```html
<div class="tablero-container">
  <div class="tablero-header">
    <h2>Tablero de Servicios</h2>
    <div class="tablero-actions">
      <button class="btn btn-primary" (click)="cargarServicios()">
        <i class="bi bi-arrow-clockwise"></i> Actualizar
      </button>
      <div class="connection-status" [class.connected]="wsService.getConnectionStatus()">
        <i class="bi" [class.bi-wifi]="wsService.getConnectionStatus()"
           [class.bi-wifi-off]="!wsService.getConnectionStatus()"></i>
        {{ wsService.getConnectionStatus() ? 'Conectado' : 'Desconectado' }}
      </div>
    </div>
  </div>

  <div *ngIf="loading" class="loading-spinner">
    <div class="spinner-border text-primary" role="status">
      <span class="visually-hidden">Cargando...</span>
    </div>
  </div>

  <div *ngIf="error" class="alert alert-danger">{{ error }}</div>

  <div class="tablero-columnas" cdkDropListGroup>
    <div *ngFor="let columna of columnas; let i = index"
         class="columna"
         [style.border-top-color]="columna.color">

      <div class="columna-header" [style.background-color]="columna.color">
        <h5>{{ columna.nombre }}</h5>
        <span class="badge">{{ columna.servicios.length }}</span>
      </div>

      <div class="columna-body"
           cdkDropList
           [id]="'columna-' + i"
           [cdkDropListData]="columna.servicios"
           [cdkDropListConnectedTo]="getConectedListIds()"
           (cdkDropListDropped)="onDrop($event)">

        <div *ngFor="let servicio of columna.servicios"
             class="servicio-card"
             cdkDrag>

          <div class="drag-handle" cdkDragHandle>
            <i class="bi bi-grip-vertical"></i>
          </div>

          <div class="servicio-numero">
            <strong>{{ servicio.numeroServicio }}</strong>
            <span *ngIf="servicio.esGarantia" class="badge bg-warning">Garantía</span>
          </div>

          <div class="servicio-cliente">
            <i class="bi bi-person"></i>
            {{ servicio.clienteNombre }}
          </div>

          <div class="servicio-equipo">
            <i class="bi bi-laptop"></i>
            {{ servicio.equipoDescripcion }}
          </div>

          <div class="servicio-fecha">
            <i class="bi bi-calendar"></i>
            {{ servicio.fechaRecepcion | date: 'dd/MM/yyyy' }}
          </div>

          <div class="servicio-footer">
            <span class="empleado">
              <i class="bi bi-person-badge"></i>
              {{ servicio.empleadoRecepcionNombre }}
            </span>
          </div>

          <!-- Vista previa mientras se arrastra -->
          <div class="drag-preview" *cdkDragPreview>
            <div class="servicio-card preview">
              <strong>{{ servicio.numeroServicio }}</strong>
              <div>{{ servicio.clienteNombre }}</div>
            </div>
          </div>
        </div>

        <div *ngIf="columna.servicios.length === 0" class="columna-vacia">
          <i class="bi bi-inbox"></i>
          <p>No hay servicios</p>
        </div>
      </div>
    </div>
  </div>
</div>
```

**src/app/components/tablero-servicios/tablero-servicios.component.scss**
```scss
.tablero-container {
  padding: 20px;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tablero-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 1.8rem;
    font-weight: 600;
  }
}

.tablero-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.connection-status {
  padding: 8px 12px;
  border-radius: 6px;
  background-color: #dc3545;
  color: white;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 6px;

  &.connected {
    background-color: #28a745;
  }

  i {
    font-size: 1.1rem;
  }
}

.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 50px;
}

.tablero-columnas {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  flex: 1;
  padding-bottom: 20px;
}

.columna {
  flex: 0 0 320px;
  display: flex;
  flex-direction: column;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-top: 4px solid;
  max-height: calc(100vh - 180px);
}

.columna-header {
  padding: 15px;
  color: white;
  border-radius: 8px 8px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;

  h5 {
    margin: 0;
    font-size: 1.1rem;
    font-weight: 600;
  }

  .badge {
    background-color: rgba(255, 255, 255, 0.3);
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 0.9rem;
  }
}

.columna-body {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  min-height: 200px;

  &.cdk-drop-list-dragging .servicio-card:not(.cdk-drag-placeholder) {
    transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
  }
}

.servicio-card {
  background: white;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: move;
  transition: box-shadow 0.2s, transform 0.2s;
  position: relative;

  &:hover {
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }

  &.cdk-drag-preview {
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
    opacity: 0.9;
  }

  &.cdk-drag-placeholder {
    opacity: 0.3;
    background: #e9ecef;
  }

  &.cdk-drag-animating {
    transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
  }
}

.drag-handle {
  position: absolute;
  top: 8px;
  right: 8px;
  color: #6c757d;
  cursor: grab;

  &:active {
    cursor: grabbing;
  }

  i {
    font-size: 1.2rem;
  }
}

.servicio-numero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-right: 25px;

  strong {
    color: #0d6efd;
    font-size: 1rem;
  }

  .badge {
    font-size: 0.75rem;
  }
}

.servicio-cliente,
.servicio-equipo,
.servicio-fecha {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #495057;
  margin-bottom: 6px;

  i {
    color: #6c757d;
  }
}

.servicio-footer {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e9ecef;
  font-size: 0.85rem;
  color: #6c757d;

  .empleado {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.columna-vacia {
  text-align: center;
  padding: 40px 20px;
  color: #adb5bd;

  i {
    font-size: 3rem;
    margin-bottom: 10px;
  }

  p {
    margin: 0;
    font-size: 0.95rem;
  }
}

.drag-preview {
  .servicio-card {
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  }
}

/* Scrollbar personalizada */
.columna-body::-webkit-scrollbar {
  width: 8px;
}

.columna-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.columna-body::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;

  &:hover {
    background: #555;
  }
}
```

### 6. Agregar Ruta

**src/app/app.routes.ts**
```typescript
{
  path: 'tablero-servicios',
  component: TableroServiciosComponent,
  canActivate: [AuthGuard]
}
```

### 7. Actualizar Module (si usas módulos)

**src/app/app.module.ts**
```typescript
import { DragDropModule } from '@angular/cdk/drag-drop';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  imports: [
    // ...otros imports
    DragDropModule,
    HttpClientModule,
  ],
})
```

---

## Características Implementadas

### Backend
1. ✅ Generación automática de número de servicio (SRV25XXXXX)
2. ✅ CRUD completo de servicios
3. ✅ Manejo de detalles de servicio (componentes)
4. ✅ WebSocket para notificaciones en tiempo real
5. ✅ Endpoint específico para cambio de estado
6. ✅ Filtros por estado, cliente, fechas
7. ✅ Sistema de garantías

### Frontend a Implementar
1. 🔲 Tablero Kanban con drag & drop
2. 🔲 Actualizaciones en tiempo real via WebSocket
3. 🔲 Indicador de conexión
4. 🔲 Tarjetas de servicio con información relevante
5. 🔲 Optimistic updates (actualización inmediata)
6. 🔲 Manejo de errores y reconexión

---

## Flujo de Trabajo

1. **Usuario arrastra servicio** → Cambia visualmente de columna
2. **Frontend llama API** → `PATCH /api/servicios/{id}/cambiar-estado`
3. **Backend actualiza** → Guarda en DB y emite evento WebSocket
4. **Todos los clientes** → Reciben el evento y actualizan su tablero

---

## Notas Importantes

- El WebSocket se conecta automáticamente al cargar el componente
- Las actualizaciones son "optimistic": se muestra el cambio inmediatamente
- Si falla el cambio de estado, se revierte visualmente
- Todos los usuarios ven los cambios en tiempo real
- El tablero se auto-organiza por estado

---

## Testing

1. Abrir múltiples pestañas del navegador
2. Arrastrar un servicio en una pestaña
3. Verificar que se actualice en todas las pestañas
4. Crear un servicio desde API y ver que aparece en tiempo real

---

## Próximos Pasos (Opcionales)

- Añadir filtros por fecha, cliente, técnico
- Implementar búsqueda de servicios
- Agregar vista de detalles al hacer click
- Implementar notificaciones toast
- Añadir métricas (cantidad por estado, tiempo promedio)
- Implementar modo oscuro
