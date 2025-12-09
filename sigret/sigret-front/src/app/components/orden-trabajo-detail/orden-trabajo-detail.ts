import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrdenTrabajoService } from '../../services/orden-trabajo.service';
import { OrdenTrabajoResponse, EstadoOrdenTrabajo, DetalleOrdenTrabajo } from '../../models/orden-trabajo.model';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BadgeModule } from 'primeng/badge';
import { DividerModule } from 'primeng/divider';
import { TagModule } from 'primeng/tag';
import { Checkbox } from 'primeng/checkbox';
import { Textarea } from 'primeng/textarea';
import { TableModule } from 'primeng/table';

@Component({
  selector: 'app-orden-trabajo-detail',
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    CardModule,
    ProgressSpinnerModule,
    BadgeModule,
    DividerModule,
    TagModule,
    Checkbox,
    Textarea,
    TableModule
  ],
  templateUrl: './orden-trabajo-detail.html',
  styleUrl: './orden-trabajo-detail.scss'
})
export class OrdenTrabajoDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly ordenTrabajoService = inject(OrdenTrabajoService);
  private readonly messageService = inject(MessageService);

  readonly ordenTrabajo = signal<OrdenTrabajoResponse | null>(null);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly procesando = signal<boolean>(false);
  readonly actualizandoDetalle = signal<number | null>(null);

  readonly EstadoOrdenTrabajo = EstadoOrdenTrabajo;

  // Computed para verificar si todos los detalles están completados
  readonly todosLosDetallesCompletados = computed(() => {
    const orden = this.ordenTrabajo();
    if (!orden || !orden.detalles || orden.detalles.length === 0) {
      return true;
    }
    return orden.detalles.every(d => d.completado);
  });

  // Computed para verificar si puede finalizar
  readonly puedeFinalizar = computed(() => {
    const orden = this.ordenTrabajo();
    return orden?.estado === EstadoOrdenTrabajo.EN_PROGRESO && this.todosLosDetallesCompletados();
  });

  // Computed para el contador de items completados
  readonly contadorItemsCompletados = computed(() => {
    const orden = this.ordenTrabajo();
    if (!orden || !orden.detalles) return '0 de 0';
    const completados = orden.detalles.filter(d => d.completado).length;
    const total = orden.detalles.length;
    return `${completados} de ${total}`;
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarOrdenTrabajo(+id);
    } else {
      this.error.set('ID de orden de trabajo no válido');
      this.loading.set(false);
    }
  }

  cargarOrdenTrabajo(id: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.ordenTrabajoService.obtenerOrdenTrabajoPorId(id).subscribe({
      next: (data) => {
        this.ordenTrabajo.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar orden de trabajo:', err);
        this.error.set('Error al cargar la orden de trabajo');
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cargar la orden de trabajo'
        });
      }
    });
  }

  iniciarOrden(): void {
    const orden = this.ordenTrabajo();
    if (!orden) return;

    this.procesando.set(true);
    this.ordenTrabajoService.iniciarOrdenTrabajo(orden.id).subscribe({
      next: (data) => {
        this.ordenTrabajo.set(data);
        this.procesando.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Orden Iniciada',
          detail: 'La orden de trabajo ha sido iniciada'
        });
      },
      error: (err) => {
        console.error('Error al iniciar orden:', err);
        this.procesando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo iniciar la orden de trabajo'
        });
      }
    });
  }

  finalizarOrden(): void {
    const orden = this.ordenTrabajo();
    if (!orden) return;

    this.procesando.set(true);
    this.ordenTrabajoService.finalizarOrdenTrabajo(orden.id).subscribe({
      next: (data) => {
        this.ordenTrabajo.set(data);
        this.procesando.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Orden Finalizada',
          detail: 'La orden de trabajo ha sido finalizada'
        });
      },
      error: (err) => {
        console.error('Error al finalizar orden:', err);
        this.procesando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo finalizar la orden de trabajo'
        });
      }
    });
  }

  navegarAServicio(): void {
    const orden = this.ordenTrabajo();
    if (orden?.servicioId) {
      this.router.navigate(['/servicios', orden.servicioId]);
    }
  }

  navegarAPresupuesto(): void {
    const orden = this.ordenTrabajo();
    if (orden?.presupuestoId) {
      this.router.navigate(['/presupuestos', orden.presupuestoId]);
    }
  }

  toggleDetalleCompletado(detalle: DetalleOrdenTrabajo): void {
    const orden = this.ordenTrabajo();
    if (!orden) return;

    const nuevoEstado = !detalle.completado;
    this.actualizandoDetalle.set(detalle.id);

    this.ordenTrabajoService.actualizarDetalleOrdenTrabajo(
      orden.id,
      detalle.id,
      detalle.comentario,
      nuevoEstado
    ).subscribe({
      next: (ordenActualizada) => {
        this.ordenTrabajo.set(ordenActualizada);
        this.actualizandoDetalle.set(null);
        this.messageService.add({
          severity: 'success',
          summary: nuevoEstado ? 'Item Completado' : 'Item Marcado Pendiente',
          detail: `${detalle.item} actualizado`
        });
      },
      error: (err) => {
        console.error('Error al actualizar detalle:', err);
        this.actualizandoDetalle.set(null);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo actualizar el item'
        });
      }
    });
  }

  actualizarComentarioDetalle(detalle: DetalleOrdenTrabajo, comentario: string): void {
    const orden = this.ordenTrabajo();
    if (!orden) return;

    this.actualizandoDetalle.set(detalle.id);

    this.ordenTrabajoService.actualizarDetalleOrdenTrabajo(
      orden.id,
      detalle.id,
      comentario,
      undefined
    ).subscribe({
      next: (ordenActualizada) => {
        this.ordenTrabajo.set(ordenActualizada);
        this.actualizandoDetalle.set(null);
        this.messageService.add({
          severity: 'success',
          summary: 'Comentario Guardado',
          detail: 'El comentario ha sido actualizado'
        });
      },
      error: (err) => {
        console.error('Error al actualizar comentario:', err);
        this.actualizandoDetalle.set(null);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo guardar el comentario'
        });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/ordenes-trabajo']);
  }

  getEstadoSeverity(estado: EstadoOrdenTrabajo): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    switch (estado) {
      case EstadoOrdenTrabajo.PENDIENTE:
        return 'warn';
      case EstadoOrdenTrabajo.EN_PROGRESO:
        return 'info';
      case EstadoOrdenTrabajo.TERMINADA:
        return 'success';
      case EstadoOrdenTrabajo.CANCELADA:
        return 'danger';
      default:
        return 'secondary';
    }
  }

  getEstadoLabel(estado: EstadoOrdenTrabajo): string {
    switch (estado) {
      case EstadoOrdenTrabajo.PENDIENTE:
        return 'Pendiente';
      case EstadoOrdenTrabajo.EN_PROGRESO:
        return 'En Progreso';
      case EstadoOrdenTrabajo.TERMINADA:
        return 'Terminada';
      case EstadoOrdenTrabajo.CANCELADA:
        return 'Cancelada';
      default:
        return estado;
    }
  }
}
