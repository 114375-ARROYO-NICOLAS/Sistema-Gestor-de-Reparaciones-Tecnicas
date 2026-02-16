import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CdkDragDrop, CdkDrag, CdkDropList, CdkDropListGroup, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { ServicioService } from '../../services/servicio.service';
import { WebSocketService } from '../../services/websocket.service';
import { ServicioList, EstadoServicio, ServicioEvent } from '../../models/servicio.model';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BadgeModule } from 'primeng/badge';
import { CardModule } from 'primeng/card';
import { Tag } from 'primeng/tag';

interface Columna {
  nombre: string;
  estado: EstadoServicio;
  color: string;
  servicios: ServicioList[];
}

@Component({
  selector: 'app-tablero-servicios',
  imports: [
    CommonModule,
    ButtonModule,
    ProgressSpinnerModule,
    BadgeModule,
    CardModule,
    Tag,
    CdkDrag,
    CdkDropList,
    CdkDropListGroup
  ],
  templateUrl: './tablero-servicios.component.html',
  styleUrls: ['./tablero-servicios.component.scss']
})
export class TableroServiciosComponent implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly servicioService = inject(ServicioService);
  private readonly wsService = inject(WebSocketService);
  private readonly messageService = inject(MessageService);

  private wsSubscription?: Subscription;

  // Signals
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly isConnected = computed(() => this.wsService.isConnected());

  // Servicios recibidos (zona superior scrolleable)
  serviciosRecibidos: ServicioList[] = [];

  // Columnas del tablero Kanban (sin la columna RECIBIDO)
  columnas: Columna[] = [
    {
      nombre: 'Presupuestado',
      estado: EstadoServicio.PRESUPUESTADO,
      color: '#ffc107',
      servicios: []
    },
    {
      nombre: 'Aprobado',
      estado: EstadoServicio.APROBADO,
      color: '#17a2b8',
      servicios: []
    },
    {
      nombre: 'En Reparación',
      estado: EstadoServicio.EN_REPARACION,
      color: '#007bff',
      servicios: []
    },
    {
      nombre: 'Terminado',
      estado: EstadoServicio.TERMINADO,
      color: '#28a745',
      servicios: []
    },
    {
      nombre: 'Rechazado',
      estado: EstadoServicio.RECHAZADO,
      color: '#dc3545',
      servicios: []
    }
  ];

  ngOnInit(): void {
    this.cargarServicios();
    this.conectarWebSocket();
  }

  ngOnDestroy(): void {
    this.wsSubscription?.unsubscribe();
    this.wsService.disconnect();
  }

  cargarServicios(): void {
    this.loading.set(true);
    this.error.set(null);

    this.servicioService.obtenerTodosLosServicios().subscribe({
      next: (servicios) => {
        // Filtrar servicios que NO son garantías
        const serviciosSinGarantias = servicios.filter(s => !s.esGarantia);
        this.organizarServiciosPorEstado(serviciosSinGarantias);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar servicios:', err);
        this.error.set('Error al cargar los servicios. Por favor, intente nuevamente.');
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los servicios'
        });
      }
    });
  }

  private organizarServiciosPorEstado(servicios: ServicioList[]): void {
    // Limpiar columnas y servicios recibidos
    this.columnas.forEach(col => col.servicios = []);
    this.serviciosRecibidos = [];

    // Distribuir servicios por estado
    servicios.forEach(servicio => {
      if (servicio.estado === EstadoServicio.RECIBIDO) {
        // Los servicios RECIBIDO van a la sección superior
        this.serviciosRecibidos.push(servicio);
      } else {
        // El resto va a sus columnas correspondientes
        const columna = this.columnas.find(col => col.estado === servicio.estado);
        if (columna) {
          columna.servicios.push(servicio);
        }
      }
    });

    // Ordenar servicios recibidos por fecha de recepción (más recientes primero)
    this.serviciosRecibidos.sort((a, b) => {
      return new Date(b.fechaRecepcion).getTime() - new Date(a.fechaRecepcion).getTime();
    });

    // Ordenar servicios de columnas por fecha de recepción (más recientes primero)
    this.columnas.forEach(col => {
      col.servicios.sort((a, b) => {
        return new Date(b.fechaRecepcion).getTime() - new Date(a.fechaRecepcion).getTime();
      });
    });
  }

  private conectarWebSocket(): void {
    this.wsService.connect();

    this.wsSubscription = this.wsService.servicioEvent$.subscribe({
      next: (event: ServicioEvent | null) => {
        if (event) {
          this.manejarEventoWebSocket(event);
        }
      },
      error: (err) => {
        console.error('Error en WebSocket:', err);
        this.messageService.add({
          severity: 'warn',
          summary: 'Conexión WebSocket',
          detail: 'Error en la conexión de tiempo real'
        });
      }
    });
  }

  private manejarEventoWebSocket(event: ServicioEvent): void {
    console.log('Manejando evento WebSocket:', event);

    switch (event.tipo) {
      case 'CREADO':
        if (event.servicio) {
          this.agregarServicio(event.servicio);
          this.messageService.add({
            severity: 'info',
            summary: 'Nuevo Servicio',
            detail: `Servicio ${event.numeroServicio} creado`
          });
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
          this.messageService.add({
            severity: 'success',
            summary: 'Estado Actualizado',
            detail: `Servicio ${event.numeroServicio} cambió de estado`
          });
        }
        break;

      case 'ELIMINADO':
        this.eliminarServicio(event.servicioId);
        this.messageService.add({
          severity: 'info',
          summary: 'Servicio Eliminado',
          detail: `Servicio ${event.numeroServicio} eliminado`
        });
        break;
    }
  }

  private agregarServicio(servicio: ServicioList): void {
    if (servicio.estado === EstadoServicio.RECIBIDO) {
      // Agregar a la sección de servicios recibidos
      const existe = this.serviciosRecibidos.some(s => s.id === servicio.id);
      if (!existe) {
        this.serviciosRecibidos.unshift(servicio);
      }
    } else {
      // Agregar a la columna correspondiente
      const columna = this.columnas.find(col => col.estado === servicio.estado);
      if (columna) {
        const existe = columna.servicios.some(s => s.id === servicio.id);
        if (!existe) {
          columna.servicios.unshift(servicio);
        }
      }
    }
  }

  private actualizarServicio(servicio: ServicioList): void {
    if (servicio.estado === EstadoServicio.RECIBIDO) {
      const index = this.serviciosRecibidos.findIndex(s => s.id === servicio.id);
      if (index !== -1) {
        this.serviciosRecibidos[index] = servicio;
      }
    } else {
      const columna = this.columnas.find(col => col.estado === servicio.estado);
      if (columna) {
        const index = columna.servicios.findIndex(s => s.id === servicio.id);
        if (index !== -1) {
          columna.servicios[index] = servicio;
        }
      }
    }
  }

  private moverServicio(servicio: ServicioList, estadoAnterior: EstadoServicio): void {
    // Eliminar del estado anterior
    if (estadoAnterior === EstadoServicio.RECIBIDO) {
      this.serviciosRecibidos = this.serviciosRecibidos.filter(s => s.id !== servicio.id);
    } else {
      const columnaAnterior = this.columnas.find(col => col.estado === estadoAnterior);
      if (columnaAnterior) {
        columnaAnterior.servicios = columnaAnterior.servicios.filter(s => s.id !== servicio.id);
      }
    }

    // Agregar al nuevo estado
    if (servicio.estado === EstadoServicio.RECIBIDO) {
      const existe = this.serviciosRecibidos.some(s => s.id === servicio.id);
      if (!existe) {
        this.serviciosRecibidos.unshift(servicio);
      }
    } else {
      const columnaNueva = this.columnas.find(col => col.estado === servicio.estado);
      if (columnaNueva) {
        const existe = columnaNueva.servicios.some(s => s.id === servicio.id);
        if (!existe) {
          columnaNueva.servicios.unshift(servicio);
        }
      }
    }
  }

  private eliminarServicio(servicioId: number): void {
    // Eliminar de servicios recibidos
    this.serviciosRecibidos = this.serviciosRecibidos.filter(s => s.id !== servicioId);

    // Eliminar de columnas
    this.columnas.forEach(columna => {
      columna.servicios = columna.servicios.filter(s => s.id !== servicioId);
    });
  }

  // Drag & Drop usando Angular CDK
  dropServicio(event: CdkDragDrop<ServicioList[]>, columnaDestino?: Columna): void {
    const servicio = event.item.data as ServicioList;
    const estadoAnterior = servicio.estado;

    // Determinar el estado destino
    let estadoDestino: EstadoServicio;

    if (event.container.id === 'servicios-recibidos') {
      estadoDestino = EstadoServicio.RECIBIDO;
    } else if (columnaDestino) {
      estadoDestino = columnaDestino.estado;
    } else {
      return;
    }

    // Si es el mismo estado, solo reordenar
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }

    // Si es diferente estado, transferir y actualizar
    if (estadoAnterior !== estadoDestino) {
      // Transferir el item entre contenedores
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );

      // Actualizar el estado del servicio
      servicio.estado = estadoDestino;

      // Cambiar estado en el backend
      this.servicioService.cambiarEstado(servicio.id, estadoDestino).subscribe({
        next: () => {
          console.log('Estado cambiado exitosamente');
          // El WebSocket se encargará de notificar a otros clientes
        },
        error: (err) => {
          console.error('Error al cambiar estado:', err);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'No se pudo cambiar el estado del servicio'
          });
          // Revertir cambio visual si falla
          this.cargarServicios();
        }
      });
    }
  }

  // Helper para obtener IDs de drop lists conectados
  getConnectedDropLists(): string[] {
    const lists = ['servicios-recibidos'];
    this.columnas.forEach(col => {
      lists.push(`columna-${col.estado}`);
    });
    return lists;
  }

  getTotalServicios(): number {
    const totalColumnas = this.columnas.reduce((total, col) => total + col.servicios.length, 0);
    return totalColumnas + this.serviciosRecibidos.length;
  }

  formatFecha(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  // Navegación a creación de servicio
  openCreateDialog(): void {
    this.router.navigate(['/servicios/nuevo']);
  }

  // Navegación al buscador de servicios
  irABuscarServicios(): void {
    this.router.navigate(['/servicios/buscar']);
  }

  // Navegación a servicios eliminados
  irAEliminados(): void {
    this.router.navigate(['/servicios/eliminados']);
  }

  // Navegación al detalle del servicio
  verDetalleServicio(servicioId: number, event?: MouseEvent): void {
    // Prevenir que el click se propague al drag and drop
    if (event) {
      event.stopPropagation();
    }
    this.router.navigate(['/servicios', servicioId]);
  }
}
