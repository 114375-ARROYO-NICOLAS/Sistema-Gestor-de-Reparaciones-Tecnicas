import { Component, OnInit, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PresupuestoPublicoService } from '../../services/presupuesto-publico.service';
import { PresupuestoPublico } from '../../models/presupuesto.model';
import { MessageService } from 'primeng/api';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { DividerModule } from 'primeng/divider';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-presupuesto-publico',
  imports: [
    CardModule,
    ButtonModule,
    ProgressSpinnerModule,
    DividerModule,
    TableModule,
    ToastModule
  ],
  templateUrl: './presupuesto-publico.component.html',
  styleUrl: './presupuesto-publico.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class PresupuestoPublicoComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(PresupuestoPublicoService);
  private readonly messageService = inject(MessageService);

  readonly presupuesto = signal<PresupuestoPublico | null>(null);
  readonly cargando = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly procesando = signal<boolean>(false);
  readonly accionCompletada = signal<boolean>(false);
  readonly mensajeExito = signal<string>('');

  readonly estaVencido = computed(() => this.presupuesto()?.vencido === true);
  readonly yaRespondido = computed(() => {
    const estado = this.presupuesto()?.estado;
    return estado === 'APROBADO' || estado === 'RECHAZADO';
  });

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token');

    if (!token) {
      this.error.set('Token inválido');
      this.cargando.set(false);
      return;
    }

    this.service.obtenerPorToken(token).subscribe({
      next: (presupuesto) => {
        this.presupuesto.set(presupuesto);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Error al cargar el presupuesto. El token puede estar expirado o ya haber sido utilizado.');
        this.cargando.set(false);
      }
    });
  }

  aprobarDirecto(precio: 'ORIGINAL' | 'ALTERNATIVO'): void {
    const token = this.route.snapshot.paramMap.get('token');
    if (!token) return;

    this.procesando.set(true);
    this.service.aprobar(token, precio)
      .pipe(finalize(() => this.procesando.set(false)))
      .subscribe({
        next: (response) => {
          this.accionCompletada.set(true);
          this.mensajeExito.set(response.message);
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: err.error?.message || 'Error al procesar la solicitud'
          });
        }
      });
  }

  rechazar(): void {
    const token = this.route.snapshot.paramMap.get('token');
    if (!token) return;

    this.procesando.set(true);
    this.service.rechazar(token)
      .pipe(finalize(() => this.procesando.set(false)))
      .subscribe({
        next: (response) => {
          this.accionCompletada.set(true);
          this.mensajeExito.set(response.message);
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: err.error?.message || 'Error al procesar la solicitud'
          });
        }
      });
  }

  formatCurrency(value: number | undefined): string {
    if (!value) return '$0,00';
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS'
    }).format(value);
  }

  formatFecha(fecha: string | undefined): string {
    if (!fecha) return '';
    const date = new Date(fecha + 'T00:00:00');
    return date.toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }
}
