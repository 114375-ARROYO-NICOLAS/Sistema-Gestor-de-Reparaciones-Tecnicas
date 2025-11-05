import { Component, OnInit, signal, computed, inject, ChangeDetectionStrategy, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ServicioService } from '../../services/servicio.service';
import { WebSocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth.service';
import { ServicioList, EstadoServicio, ServicioEvent } from '../../models/servicio.model';
import { ServicioUpdateDto } from '../../models/servicio-update.dto';
import { MessageService } from 'primeng/api';
import { Card } from 'primeng/card';
import { Button } from 'primeng/button';
import { Tag } from 'primeng/tag';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Toast } from 'primeng/toast';
import { Tooltip } from 'primeng/tooltip';
import { Subscription } from 'rxjs';

interface ColumnaKanban {
  titulo: string;
  estado: EstadoServicio;
  servicios: ServicioList[];
  color: string;
  icon: string;
}

@Component({
  selector: 'app-tablero-garantias',
  imports: [
    CommonModule,
    Card,
    Button,
    Tag,
    ProgressSpinner,
    Toast,
    Tooltip
  ],
  templateUrl: './tablero-garantias.html',
  styleUrl: './tablero-garantias.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class TableroGarantiasComponent implements OnInit, OnDestroy {
  private readonly servicioService = inject(ServicioService);
  private readonly websocketService = inject(WebSocketService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);

  // Signals
  readonly loading = signal<boolean>(true);
  readonly servicios = signal<ServicioList[]>([]);
  readonly servicioArrastrado = signal<ServicioList | null>(null);
  readonly columnaHover = signal<string | null>(null);

  // Computed - Estadísticas totales
  readonly totalGarantias = computed(() => this.servicios().length);
  readonly pendientesEvaluacion = computed(() =>
    this.servicios().filter(s => s.estado === EstadoServicio.ESPERANDO_EVALUACION_GARANTIA).length
  );
  readonly enReparacion = computed(() =>
    this.servicios().filter(s => s.estado === EstadoServicio.EN_REPARACION).length
  );

  // WebSocket subscription
  private wsSubscription?: Subscription;

  // Definición de columnas del Kanban
  readonly columnas: ColumnaKanban[] = [
    {
      titulo: 'Esperando Evaluación',
      estado: EstadoServicio.ESPERANDO_EVALUACION_GARANTIA,
      servicios: [],
      color: 'text-orange-600',
      icon: 'pi-clock'
    },
    {
      titulo: 'En Reparación',
      estado: EstadoServicio.EN_REPARACION,
      servicios: [],
      color: 'text-blue-600',
      icon: 'pi-wrench'
    },
    {
      titulo: 'Terminado',
      estado: EstadoServicio.TERMINADO,
      servicios: [],
      color: 'text-green-600',
      icon: 'pi-check-circle'
    },
    {
      titulo: 'Sin Reparación',
      estado: EstadoServicio.GARANTIA_SIN_REPARACION,
      servicios: [],
      color: 'text-cyan-600',
      icon: 'pi-info-circle'
    },
    {
      titulo: 'Garantía Rechazada',
      estado: EstadoServicio.GARANTIA_RECHAZADA,
      servicios: [],
      color: 'text-red-600',
      icon: 'pi-times-circle'
    }
  ];

  // Computed para garantías recibidas (estado RECIBIDO)
  readonly garantiasRecibidas = computed(() =>
    this.servicios().filter(s => s.estado === EstadoServicio.RECIBIDO)
  );

  // Computed para columnas con servicios
  readonly columnasConServicios = computed(() => {
    const serviciosList = this.servicios();
    return this.columnas.map(columna => ({
      ...columna,
      servicios: serviciosList.filter(s => s.estado === columna.estado)
    }));
  });

  ngOnInit(): void {
    this.cargarGarantias();
    this.suscribirseAWebSocket();
  }

  ngOnDestroy(): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
  }

  cargarGarantias(): void {
    this.loading.set(true);

    this.servicioService.obtenerServiciosGarantia().subscribe({
      next: (servicios) => {
        this.servicios.set(servicios);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error al cargar garantías:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar las garantías'
        });
        this.loading.set(false);
      }
    });
  }

  suscribirseAWebSocket(): void {
    this.wsSubscription = this.websocketService.servicioEvent$.subscribe({
      next: (event: ServicioEvent | null) => {
        if (!event) return;

        console.log('Evento de servicio recibido:', event);

        if (event.tipo === 'CREADO' && event.servicio) {
          // Solo agregar si es garantía
          if (event.servicio.esGarantia) {
            this.servicios.update(servicios => [...servicios, event.servicio!]);
          }
        } else if (event.tipo === 'ACTUALIZADO' || event.tipo === 'ESTADO_CAMBIADO') {
          // Actualizar servicio existente
          this.servicios.update(servicios =>
            servicios.map(s => s.id === event.servicioId && event.servicio ? event.servicio : s)
          );
        } else if (event.tipo === 'ELIMINADO') {
          // Eliminar servicio
          this.servicios.update(servicios =>
            servicios.filter(s => s.id !== event.servicioId)
          );
        }
      },
      error: (error: unknown) => {
        console.error('Error en WebSocket:', error);
      }
    });
  }

  // Drag and Drop handlers
  onDragStart(servicio: ServicioList, event: DragEvent): void {
    this.servicioArrastrado.set(servicio);
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('text/plain', servicio.id.toString());
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
  }

  onDragEnter(columna: ColumnaKanban): void {
    this.columnaHover.set(columna.estado);
  }

  onDragLeave(): void {
    this.columnaHover.set(null);
  }

  onDrop(columna: ColumnaKanban, event: DragEvent): void {
    event.preventDefault();
    this.columnaHover.set(null);

    const servicio = this.servicioArrastrado();
    if (!servicio) return;

    const nuevoEstado = columna.estado;

    // No hacer nada si es el mismo estado
    if (servicio.estado === nuevoEstado) {
      this.servicioArrastrado.set(null);
      return;
    }

    // Validar transiciones permitidas
    if (!this.esTransicionValida(servicio.estado, nuevoEstado)) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Transición no válida',
        detail: `No se puede mover de ${servicio.estado} a ${nuevoEstado}`
      });
      this.servicioArrastrado.set(null);
      return;
    }

    // Cambiar estado
    this.cambiarEstado(servicio, nuevoEstado);
    this.servicioArrastrado.set(null);
  }

  esTransicionValida(estadoActual: EstadoServicio, estadoNuevo: EstadoServicio): boolean {
    // Definir transiciones válidas
    const transicionesValidas: Record<string, EstadoServicio[]> = {
      [EstadoServicio.ESPERANDO_EVALUACION_GARANTIA]: [
        EstadoServicio.EN_REPARACION,
        EstadoServicio.GARANTIA_SIN_REPARACION,
        EstadoServicio.GARANTIA_RECHAZADA
      ],
      [EstadoServicio.EN_REPARACION]: [
        EstadoServicio.TERMINADO,
        EstadoServicio.ESPERANDO_EVALUACION_GARANTIA // Puede volver
      ],
      [EstadoServicio.TERMINADO]: [],
      [EstadoServicio.GARANTIA_SIN_REPARACION]: [],
      [EstadoServicio.GARANTIA_RECHAZADA]: []
    };

    return transicionesValidas[estadoActual]?.includes(estadoNuevo) || false;
  }

  cambiarEstado(servicio: ServicioList, nuevoEstado: EstadoServicio): void {
    // Obtener el empleado logueado
    const currentUser = this.authService.getCurrentUser();

    if (!currentUser || !currentUser.empleadoId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo identificar al empleado logueado'
      });
      return;
    }

    // Preparar el DTO de actualización con estado y técnico
    const updateDto: ServicioUpdateDto = {
      estado: nuevoEstado,
      tecnicoEvaluacionId: currentUser.empleadoId
    };

    // Actualizar el servicio con estado y asignación de técnico
    this.servicioService.actualizarServicio(servicio.id, updateDto).subscribe({
      next: (servicioActualizado) => {
        // Actualizar el servicio en la lista
        this.servicios.update(servicios =>
          servicios.map(s => s.id === servicio.id ? {
            ...s,
            estado: servicioActualizado.estado
          } : s)
        );

        this.messageService.add({
          severity: 'success',
          summary: 'Estado actualizado',
          detail: `Garantía ${servicio.numeroServicio} asignada y movida a ${nuevoEstado}`,
          life: 3000
        });
      },
      error: (error) => {
        console.error('Error al cambiar estado:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cambiar el estado de la garantía'
        });
      }
    });
  }

  verDetalle(servicioId: number): void {
    this.router.navigate(['/servicios', servicioId]);
  }

  getEstadoSeverity(estado: EstadoServicio): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    const severityMap: Record<string, 'success' | 'info' | 'warn' | 'danger' | 'secondary'> = {
      'ESPERANDO_EVALUACION_GARANTIA': 'warn',
      'EN_REPARACION': 'info',
      'TERMINADO': 'success',
      'GARANTIA_SIN_REPARACION': 'info',
      'GARANTIA_RECHAZADA': 'danger'
    };
    return severityMap[estado] || 'secondary';
  }

  formatFecha(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }
}
