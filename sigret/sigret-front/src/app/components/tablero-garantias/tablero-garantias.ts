import { Component, OnInit, signal, computed, inject, ChangeDetectionStrategy, OnDestroy, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CdkDropListGroup, CdkDropList, CdkDrag, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { ServicioService } from '../../services/servicio.service';
import { OrdenTrabajoService } from '../../services/orden-trabajo.service';
import { WebSocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth.service';
import { ServicioList, EstadoServicio, ServicioEvent } from '../../models/servicio.model';
import { ServicioUpdateDto } from '../../models/servicio-update.dto';
import { MessageService } from 'primeng/api';
import { Card } from 'primeng/card';
import { Button } from 'primeng/button';
import { Tag } from 'primeng/tag';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Subscription } from 'rxjs';
import { EvaluacionGarantiaDialog, ResultadoEvaluacion, EvaluacionResult } from '../evaluacion-garantia-dialog/evaluacion-garantia-dialog';

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
    CdkDropListGroup,
    CdkDropList,
    CdkDrag,
    Card,
    Button,
    Tag,
    ProgressSpinner,
    EvaluacionGarantiaDialog
  ],
  templateUrl: './tablero-garantias.html',
  styleUrl: './tablero-garantias.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TableroGarantiasComponent implements OnInit, OnDestroy {
  private readonly servicioService = inject(ServicioService);
  private readonly ordenTrabajoService = inject(OrdenTrabajoService);
  private readonly websocketService = inject(WebSocketService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);

  // Signals
  readonly loading = signal<boolean>(true);
  readonly servicios = signal<ServicioList[]>([]);

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

  // Diálogo de evaluación
  readonly evaluacionDialog = viewChild.required<EvaluacionGarantiaDialog>('evaluacionDialog');

  // Estado temporal para el servicio que está siendo evaluado
  private servicioEnEvaluacion?: ServicioList;
  private estadoDestino?: EstadoServicio;

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

  // Método para obtener IDs de todas las drop lists conectadas
  getConnectedDropLists(): string[] {
    const columnIds = this.columnas.map(col => `columna-${col.estado}`);
    return ['servicios-recibidos', ...columnIds];
  }

  // CDK Drag and Drop handler
  dropServicio(event: CdkDragDrop<ServicioList[]>, columna?: ColumnaKanban): void {
    const servicio = event.item.data as ServicioList;

    // Si se suelta en el mismo contenedor, reorganizar
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }

    // Determinar el nuevo estado
    let nuevoEstado: EstadoServicio;

    if (columna) {
      nuevoEstado = columna.estado;
    } else {
      // Si no hay columna, es porque se soltó en servicios-recibidos
      nuevoEstado = EstadoServicio.RECIBIDO;
    }

    // No hacer nada si es el mismo estado
    if (servicio.estado === nuevoEstado) {
      return;
    }

    // Validar transiciones permitidas
    if (!this.esTransicionValida(servicio.estado, nuevoEstado)) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Transición no válida',
        detail: `No se puede mover de ${servicio.estado} a ${nuevoEstado}`
      });
      return;
    }

    // Verificar si necesita evaluación (cuando viene de ESPERANDO_EVALUACION_GARANTIA)
    if (servicio.estado === EstadoServicio.ESPERANDO_EVALUACION_GARANTIA) {
      this.servicioEnEvaluacion = servicio;
      this.estadoDestino = nuevoEstado;

      // Determinar el tipo de evaluación según el estado destino
      let tipoEvaluacion: ResultadoEvaluacion;

      if (nuevoEstado === EstadoServicio.EN_REPARACION) {
        tipoEvaluacion = 'CUMPLE';
      } else if (nuevoEstado === EstadoServicio.GARANTIA_RECHAZADA) {
        tipoEvaluacion = 'NO_CUMPLE';
      } else if (nuevoEstado === EstadoServicio.GARANTIA_SIN_REPARACION) {
        tipoEvaluacion = 'SIN_REPARACION';
      } else {
        // No debería llegar aquí, pero por si acaso
        this.cambiarEstado(servicio, nuevoEstado);
        return;
      }

      // Abrir el diálogo de evaluación
      this.evaluacionDialog().open(servicio, tipoEvaluacion);
    } else {
      // Si no necesita evaluación, cambiar estado directamente
      this.cambiarEstado(servicio, nuevoEstado);
    }
  }

  esTransicionValida(estadoActual: EstadoServicio, estadoNuevo: EstadoServicio): boolean {
    // Definir transiciones válidas
    const transicionesValidas: Record<string, EstadoServicio[]> = {
      [EstadoServicio.RECIBIDO]: [
        EstadoServicio.ESPERANDO_EVALUACION_GARANTIA
      ],
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
        // Recargar todas las garantías para obtener los campos actualizados (incluido técnico asignado)
        this.servicioService.obtenerServiciosGarantia().subscribe({
          next: (servicios) => {
            this.servicios.set(servicios);
          }
        });

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

  /**
   * Maneja la confirmación de la evaluación de garantía
   */
  onEvaluacionConfirmada(resultado: EvaluacionResult): void {
    if (!this.servicioEnEvaluacion || !this.estadoDestino) {
      return;
    }

    const servicio = this.servicioEnEvaluacion;
    const nuevoEstado = this.estadoDestino;

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

    // Preparar el DTO según el resultado
    const updateDto: ServicioUpdateDto = {
      estado: nuevoEstado,
      tecnicoEvaluacionId: currentUser.empleadoId
    };

    // Agregar información específica según el tipo de evaluación
    if (resultado.resultado === 'CUMPLE') {
      updateDto.garantiaCumpleCondiciones = true;
      updateDto.itemsEvaluacionGarantia = resultado.itemsSeleccionados;
      updateDto.observacionesEvaluacionGarantia = 'Garantía aceptada - Items con falla identificados';
    } else if (resultado.resultado === 'NO_CUMPLE') {
      updateDto.garantiaCumpleCondiciones = false;
      updateDto.observacionesEvaluacionGarantia = resultado.observaciones;
    } else if (resultado.resultado === 'SIN_REPARACION') {
      updateDto.garantiaCumpleCondiciones = true; // Cumple pero no necesita reparación
      updateDto.observacionesEvaluacionGarantia = resultado.observaciones;
    }

    // Actualizar el servicio
    this.servicioService.actualizarServicio(servicio.id, updateDto).subscribe({
      next: () => {
        // Si cumple condiciones, crear orden de trabajo automáticamente
        if (resultado.resultado === 'CUMPLE') {
          this.crearOrdenTrabajoGarantia(servicio, resultado);
        } else {
          // Para los otros casos, solo recargar
          this.recargarGarantias();
          this.mostrarMensajeExito(resultado.resultado, servicio);
        }
      },
      error: (error) => {
        console.error('Error al evaluar garantía:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo procesar la evaluación de garantía'
        });
      }
    });

    // Limpiar estado temporal
    this.servicioEnEvaluacion = undefined;
    this.estadoDestino = undefined;
  }

  /**
   * Crea una orden de trabajo sin costo para la garantía
   */
  private crearOrdenTrabajoGarantia(servicio: ServicioList, resultado: EvaluacionResult): void {
    const currentUser = this.authService.getCurrentUser();
    if (!currentUser?.empleadoId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo identificar el empleado actual',
        life: 3000
      });
      return;
    }

    // Crear observaciones básicas
    const observaciones = resultado.observaciones || 'Garantía aceptada';

    // Llamar al servicio para crear la orden de trabajo con los items seleccionados
    this.ordenTrabajoService.crearOrdenTrabajoGarantia(
      servicio.id,
      currentUser.empleadoId,
      observaciones,
      resultado.itemsSeleccionados
    ).subscribe({
      next: (ordenCreada) => {
        this.recargarGarantias();
        this.messageService.add({
          severity: 'success',
          summary: 'Orden de Trabajo Creada',
          detail: `Orden de trabajo sin costo creada para garantía ${servicio.numeroServicio}`,
          life: 5000
        });
      },
      error: (error) => {
        console.error('Error al crear orden de trabajo:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo crear la orden de trabajo para la garantía',
          life: 5000
        });
      }
    });
  }

  /**
   * Maneja la cancelación de la evaluación
   */
  onEvaluacionCancelada(): void {
    this.servicioEnEvaluacion = undefined;
    this.estadoDestino = undefined;

    this.messageService.add({
      severity: 'info',
      summary: 'Evaluación cancelada',
      detail: 'La evaluación de garantía fue cancelada',
      life: 3000
    });
  }

  /**
   * Recarga las garantías del backend
   */
  private recargarGarantias(): void {
    this.servicioService.obtenerServiciosGarantia().subscribe({
      next: (servicios) => {
        this.servicios.set(servicios);
      }
    });
  }

  /**
   * Muestra mensaje de éxito según el tipo de evaluación
   */
  private mostrarMensajeExito(resultado: ResultadoEvaluacion, servicio: ServicioList): void {
    let mensaje = '';

    if (resultado === 'NO_CUMPLE') {
      mensaje = `Garantía ${servicio.numeroServicio} rechazada`;
    } else if (resultado === 'SIN_REPARACION') {
      mensaje = `Garantía ${servicio.numeroServicio} marcada como sin reparación`;
    }

    this.messageService.add({
      severity: 'success',
      summary: 'Evaluación Completada',
      detail: mensaje,
      life: 3000
    });
  }
}
