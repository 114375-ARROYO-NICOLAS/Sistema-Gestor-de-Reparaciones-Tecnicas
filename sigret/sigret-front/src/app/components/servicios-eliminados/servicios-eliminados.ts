import { Component, OnInit, signal, inject, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ServicioService } from '../../services/servicio.service';
import { ServicioList, EstadoServicio } from '../../models/servicio.model';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { Button } from 'primeng/button';
import { Tag } from 'primeng/tag';
import { TableModule } from 'primeng/table';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Tooltip } from 'primeng/tooltip';

@Component({
  selector: 'app-servicios-eliminados',
  imports: [
    CommonModule,
    Button,
    Tag,
    TableModule,
    ProgressSpinner,
    ConfirmDialog,
    Tooltip
  ],
  providers: [ConfirmationService],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './servicios-eliminados.html'
})
export class ServiciosEliminadosComponent implements OnInit {
  private readonly servicioService = inject(ServicioService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly router = inject(Router);

  readonly loading = signal<boolean>(true);
  readonly servicios = signal<ServicioList[]>([]);
  readonly restaurando = signal<number | null>(null);

  readonly estadoSeverityMap: Record<EstadoServicio, 'success' | 'info' | 'warn' | 'danger' | 'secondary'> = {
    [EstadoServicio.RECIBIDO]: 'info',
    [EstadoServicio.ESPERANDO_EVALUACION_GARANTIA]: 'warn',
    [EstadoServicio.PRESUPUESTADO]: 'warn',
    [EstadoServicio.APROBADO]: 'info',
    [EstadoServicio.EN_REPARACION]: 'info',
    [EstadoServicio.TERMINADO]: 'success',
    [EstadoServicio.RECHAZADO]: 'danger',
    [EstadoServicio.GARANTIA_SIN_REPARACION]: 'secondary',
    [EstadoServicio.GARANTIA_RECHAZADA]: 'danger',
    [EstadoServicio.FINALIZADO]: 'success'
  };

  readonly estadoLabelMap: Record<EstadoServicio, string> = {
    [EstadoServicio.RECIBIDO]: 'Recibido',
    [EstadoServicio.ESPERANDO_EVALUACION_GARANTIA]: 'Esperando Evaluación',
    [EstadoServicio.PRESUPUESTADO]: 'Presupuestado',
    [EstadoServicio.APROBADO]: 'Aprobado',
    [EstadoServicio.EN_REPARACION]: 'En Reparación',
    [EstadoServicio.TERMINADO]: 'Terminado',
    [EstadoServicio.RECHAZADO]: 'Rechazado',
    [EstadoServicio.GARANTIA_SIN_REPARACION]: 'Garantía Sin Reparación',
    [EstadoServicio.GARANTIA_RECHAZADA]: 'Garantía Rechazada',
    [EstadoServicio.FINALIZADO]: 'Finalizado'
  };

  ngOnInit(): void {
    this.cargarServicios();
  }

  cargarServicios(): void {
    this.loading.set(true);
    this.servicioService.obtenerServiciosEliminados().subscribe({
      next: (servicios) => {
        this.servicios.set(servicios);
        this.loading.set(false);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al cargar servicios eliminados'
        });
        this.loading.set(false);
      }
    });
  }

  confirmarRestauracion(servicio: ServicioList): void {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea restaurar el servicio ${servicio.numeroServicio}?`,
      header: 'Confirmar Restauración',
      icon: 'pi pi-refresh',
      acceptLabel: 'Sí, restaurar',
      rejectLabel: 'Cancelar',
      accept: () => this.restaurarServicio(servicio)
    });
  }

  private restaurarServicio(servicio: ServicioList): void {
    this.restaurando.set(servicio.id);
    this.servicioService.restaurarServicio(servicio.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Servicio Restaurado',
          detail: `El servicio ${servicio.numeroServicio} fue restaurado correctamente`
        });
        this.cargarServicios();
        this.restaurando.set(null);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.error?.message || 'Error al restaurar el servicio'
        });
        this.restaurando.set(null);
      }
    });
  }

  verDetalle(id: number): void {
    this.router.navigate(['/servicios', id]);
  }

  getEstadoLabel(estado: EstadoServicio): string {
    return this.estadoLabelMap[estado] || estado;
  }

  getEstadoSeverity(estado: EstadoServicio): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    return this.estadoSeverityMap[estado] || 'secondary';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
