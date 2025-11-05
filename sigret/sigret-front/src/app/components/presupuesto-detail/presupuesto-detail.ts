import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PresupuestoService } from '../../services/presupuesto.service';
import { Presupuesto, EstadoPresupuesto } from '../../models/presupuesto.model';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BadgeModule } from 'primeng/badge';
import { DividerModule } from 'primeng/divider';

@Component({
  selector: 'app-presupuesto-detail',
  imports: [
    CommonModule,
    ButtonModule,
    CardModule,
    ProgressSpinnerModule,
    BadgeModule,
    DividerModule
  ],
  templateUrl: './presupuesto-detail.html',
  styleUrl: './presupuesto-detail.scss'
})
export class PresupuestoDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly presupuestoService = inject(PresupuestoService);
  private readonly messageService = inject(MessageService);

  readonly presupuesto = signal<Presupuesto | null>(null);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);

  readonly EstadoPresupuesto = EstadoPresupuesto;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadPresupuesto(+id);
    } else {
      this.error.set('ID de presupuesto no válido');
      this.loading.set(false);
    }
  }

  loadPresupuesto(id: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.presupuestoService.obtenerPresupuestoPorId(id).subscribe({
      next: (presupuesto) => {
        this.presupuesto.set(presupuesto);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading presupuesto:', err);
        this.error.set('No se pudo cargar el presupuesto');
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cargar el presupuesto'
        });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/presupuestos']);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-UY', {
      style: 'currency',
      currency: 'UYU'
    }).format(amount);
  }

  getEstadoBadgeSeverity(estado: EstadoPresupuesto): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    switch (estado) {
      case EstadoPresupuesto.PENDIENTE:
        return 'warn';
      case EstadoPresupuesto.EN_CURSO:
        return 'info';
      case EstadoPresupuesto.APROBADO:
        return 'success';
      case EstadoPresupuesto.RECHAZADO:
        return 'danger';
      default:
        return 'secondary';
    }
  }

  getEstadoLabel(estado: EstadoPresupuesto): string {
    switch (estado) {
      case EstadoPresupuesto.PENDIENTE:
        return 'Pendiente';
      case EstadoPresupuesto.EN_CURSO:
        return 'En Curso';
      case EstadoPresupuesto.APROBADO:
        return 'Aprobado';
      case EstadoPresupuesto.RECHAZADO:
        return 'Rechazado';
      default:
        return estado;
    }
  }
}
