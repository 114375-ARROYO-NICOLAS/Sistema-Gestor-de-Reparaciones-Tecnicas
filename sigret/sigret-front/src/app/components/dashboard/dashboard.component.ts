import { Component, OnInit, OnDestroy, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, interval, Subject } from 'rxjs';
import { switchMap, debounceTime } from 'rxjs/operators';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { SkeletonModule } from 'primeng/skeleton';
import { TooltipModule } from 'primeng/tooltip';
import { DatePicker } from 'primeng/datepicker';
import { UIChart } from 'primeng/chart';
import { DashboardService } from '../../services/dashboard.service';
import { ThemeService } from '../../services/theme.service';
import { WebSocketService } from '../../services/websocket.service';
import { DashboardEstadisticas } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  imports: [
    FormsModule,
    CardModule,
    ButtonModule,
    SkeletonModule,
    TooltipModule,
    DatePicker,
    UIChart,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit, OnDestroy {
  private readonly dashboardService = inject(DashboardService);
  private readonly themeService = inject(ThemeService);
  private readonly wsService = inject(WebSocketService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly estadisticas = signal<DashboardEstadisticas | null>(null);

  // Date filter signals
  fechaDesde = signal<Date | null>(null);
  fechaHasta = signal<Date | null>(null);
  readonly tieneFiltroDeFechas = computed(() => this.fechaDesde() !== null && this.fechaHasta() !== null);

  readonly isDarkMode = computed(() => this.themeService.isDarkMode());

  // KPI computed signals
  readonly serviciosActivos = computed(() => this.estadisticas()?.serviciosActivos ?? 0);
  readonly presupuestosPendientes = computed(() => this.estadisticas()?.presupuestosPendientes ?? 0);
  readonly ordenesEnProgreso = computed(() => this.estadisticas()?.ordenesEnProgreso ?? 0);
  readonly tasaAprobacion = computed(() => this.estadisticas()?.tasaAprobacionPresupuestos ?? 0);
  readonly serviciosCompletadosMes = computed(() => this.estadisticas()?.serviciosCompletadosMes ?? 0);
  readonly labelCompletados = computed(() => this.tieneFiltroDeFechas() ? 'Completados (período)' : 'Completados este Mes');

  // Chart data (reactive to data + dark mode changes)
  readonly serviciosDoughnutData = computed(() => this.buildServiciosDoughnutData());
  readonly presupuestosBarData = computed(() => this.buildPresupuestosBarData());
  readonly tendenciaLineData = computed(() => this.buildTendenciaLineData());
  readonly ordenesPieData = computed(() => this.buildOrdenesPieData());
  readonly ordenesPorEmpleadoData = computed(() => this.buildOrdenesPorEmpleadoData());
  readonly garantiasPorTipoData = computed(() => this.buildGarantiasPorTipoData());
  readonly chartOptions = computed(() => this.buildChartOptions());
  readonly barChartOptions = computed(() => this.buildBarChartOptions());
  readonly horizontalBarOptions = computed(() => this.buildHorizontalBarOptions());

  // Skeleton placeholder arrays
  readonly skeletonCards = [1, 2, 3, 4, 5];
  readonly skeletonCharts = [1, 2];

  private subscriptions: Subscription[] = [];
  private refreshTrigger = new Subject<void>();

  ngOnInit(): void {
    this.cargarEstadisticas();
    this.setupDebouncedRefresh();
    this.setupAutoRefresh();
    this.setupWebSocketRefresh();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
    this.refreshTrigger.complete();
  }

  cargarEstadisticas(): void {
    this.loading.set(true);
    this.error.set(null);

    const desde = this.fechaDesde();
    const hasta = this.fechaHasta();
    const fechaDesdeStr = desde ? this.formatDate(desde) : undefined;
    const fechaHastaStr = hasta ? this.formatDate(hasta) : undefined;

    const sub = this.dashboardService.obtenerEstadisticas(fechaDesdeStr, fechaHastaStr).subscribe({
      next: (data) => {
        this.estadisticas.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message || 'Error al cargar estadísticas');
        this.loading.set(false);
      }
    });
    this.subscriptions.push(sub);
  }

  onFechaChange(): void {
    if ((this.fechaDesde() && this.fechaHasta()) || (!this.fechaDesde() && !this.fechaHasta())) {
      this.cargarEstadisticas();
    }
  }

  limpiarFiltroFechas(): void {
    this.fechaDesde.set(null);
    this.fechaHasta.set(null);
    this.cargarEstadisticas();
  }

  private formatDate(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  private setupDebouncedRefresh(): void {
    const sub = this.refreshTrigger.pipe(
      debounceTime(2000),
      switchMap(() => {
        const desde = this.fechaDesde();
        const hasta = this.fechaHasta();
        return this.dashboardService.obtenerEstadisticas(
          desde ? this.formatDate(desde) : undefined,
          hasta ? this.formatDate(hasta) : undefined
        );
      })
    ).subscribe({
      next: (data) => this.estadisticas.set(data),
      error: () => { /* silently ignore ws-triggered refresh errors */ }
    });
    this.subscriptions.push(sub);
  }

  private setupAutoRefresh(): void {
    const sub = interval(300_000).pipe(
      switchMap(() => {
        const desde = this.fechaDesde();
        const hasta = this.fechaHasta();
        return this.dashboardService.obtenerEstadisticas(
          desde ? this.formatDate(desde) : undefined,
          hasta ? this.formatDate(hasta) : undefined
        );
      })
    ).subscribe({
      next: (data) => this.estadisticas.set(data),
      error: () => { /* silently ignore auto-refresh errors */ }
    });
    this.subscriptions.push(sub);
  }

  private setupWebSocketRefresh(): void {
    const s1 = this.wsService.servicioEvent$.subscribe(event => {
      if (event) this.refreshTrigger.next();
    });
    const s2 = this.wsService.presupuestoEvent$.subscribe(event => {
      if (event) this.refreshTrigger.next();
    });
    const s3 = this.wsService.ordenTrabajoEvent$.subscribe(event => {
      if (event) this.refreshTrigger.next();
    });
    this.subscriptions.push(s1, s2, s3);
  }

  navegarServicios(): void { this.router.navigate(['/servicios']); }
  navegarPresupuestos(): void { this.router.navigate(['/presupuestos']); }
  navegarOrdenes(): void { this.router.navigate(['/ordenes-trabajo']); }

  // --- Chart data builders ---

  private buildServiciosDoughnutData(): unknown {
    const data = this.estadisticas()?.serviciosPorEstado;
    if (!data) return {};
    this.isDarkMode();
    const entries = Object.entries(data).filter(([, v]) => v > 0);
    if (entries.length === 0) return {};
    return {
      labels: entries.map(([k]) => this.labelServicio(k)),
      datasets: [{
        data: entries.map(([, v]) => v),
        backgroundColor: entries.map(([k]) => this.colorServicio(k)),
        hoverBackgroundColor: entries.map(([k]) => this.colorServicio(k)),
      }]
    };
  }

  private buildPresupuestosBarData(): unknown {
    const data = this.estadisticas()?.presupuestosPorEstado;
    if (!data) return {};
    this.isDarkMode();
    const entries = Object.entries(data);
    return {
      labels: entries.map(([k]) => this.labelPresupuesto(k)),
      datasets: [{
        label: 'Presupuestos',
        data: entries.map(([, v]) => v),
        backgroundColor: entries.map(([k]) => this.colorPresupuesto(k)),
      }]
    };
  }

  private buildTendenciaLineData(): unknown {
    const tendencia = this.estadisticas()?.tendenciaMensual;
    if (!tendencia) return {};
    this.isDarkMode();
    return {
      labels: tendencia.map(t => t.label),
      datasets: [{
        label: 'Servicios Creados',
        data: tendencia.map(t => t.cantidad),
        fill: true,
        borderColor: '#42A5F5',
        backgroundColor: 'rgba(66, 165, 245, 0.15)',
        tension: 0.4,
        pointBackgroundColor: '#42A5F5',
        pointRadius: 5,
        pointHoverRadius: 7,
      }]
    };
  }

  private buildOrdenesPieData(): unknown {
    const data = this.estadisticas()?.ordenesTrabajoPorEstado;
    if (!data) return {};
    this.isDarkMode();
    const entries = Object.entries(data).filter(([, v]) => v > 0);
    if (entries.length === 0) return {};
    return {
      labels: entries.map(([k]) => this.labelOrden(k)),
      datasets: [{
        data: entries.map(([, v]) => v),
        backgroundColor: entries.map(([k]) => this.colorOrden(k)),
        hoverBackgroundColor: entries.map(([k]) => this.colorOrden(k)),
      }]
    };
  }

  private buildOrdenesPorEmpleadoData(): unknown {
    const data = this.estadisticas()?.ordenesPorEmpleado;
    if (!data || Object.keys(data).length === 0) return {};
    this.isDarkMode();
    const entries = Object.entries(data);
    const colors = ['#42A5F5', '#66BB6A', '#FFA726', '#AB47BC', '#26C6DA', '#EF5350', '#78909C', '#FF7043'];
    return {
      labels: entries.map(([k]) => k),
      datasets: [{
        label: 'Órdenes',
        data: entries.map(([, v]) => v),
        backgroundColor: entries.map((_, i) => colors[i % colors.length]),
      }]
    };
  }

  private buildGarantiasPorTipoData(): unknown {
    const data = this.estadisticas()?.garantiasPorTipoEquipo;
    if (!data || Object.keys(data).length === 0) return {};
    this.isDarkMode();
    const entries = Object.entries(data);
    const colors = ['#EF5350', '#AB47BC', '#42A5F5', '#26C6DA', '#66BB6A', '#FFA726', '#FF7043', '#78909C'];
    return {
      labels: entries.map(([k]) => k),
      datasets: [{
        label: 'Garantías',
        data: entries.map(([, v]) => v),
        backgroundColor: entries.map((_, i) => colors[i % colors.length]),
      }]
    };
  }

  private buildChartOptions(): unknown {
    const textColor = this.isDarkMode() ? '#eee' : '#495057';
    return {
      plugins: {
        legend: {
          labels: { color: textColor, usePointStyle: true, padding: 16 }
        }
      },
      responsive: true,
      maintainAspectRatio: false,
    };
  }

  private buildBarChartOptions(): unknown {
    const textColor = this.isDarkMode() ? '#eee' : '#495057';
    const gridColor = this.isDarkMode() ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)';
    return {
      plugins: {
        legend: {
          labels: { color: textColor, usePointStyle: true, padding: 16 }
        }
      },
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          ticks: { color: textColor },
          grid: { color: gridColor }
        },
        y: {
          ticks: { color: textColor, stepSize: 1 },
          grid: { color: gridColor },
          beginAtZero: true
        }
      }
    };
  }

  private buildHorizontalBarOptions(): unknown {
    const textColor = this.isDarkMode() ? '#eee' : '#495057';
    const gridColor = this.isDarkMode() ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)';
    return {
      indexAxis: 'y',
      plugins: {
        legend: {
          labels: { color: textColor, usePointStyle: true, padding: 16 }
        }
      },
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          ticks: { color: textColor, stepSize: 1 },
          grid: { color: gridColor },
          beginAtZero: true
        },
        y: {
          ticks: { color: textColor },
          grid: { color: gridColor }
        }
      }
    };
  }

  // --- Label/color mappings ---

  private labelServicio(estado: string): string {
    const map: Record<string, string> = {
      RECIBIDO: 'Recibido',
      ESPERANDO_EVALUACION_GARANTIA: 'Esp. Eval. Garantía',
      PRESUPUESTADO: 'Presupuestado',
      APROBADO: 'Aprobado',
      EN_REPARACION: 'En Reparación',
      TERMINADO: 'Terminado',
      RECHAZADO: 'Rechazado',
      GARANTIA_SIN_REPARACION: 'Gtía. Sin Reparación',
      GARANTIA_RECHAZADA: 'Gtía. Rechazada',
    };
    return map[estado] ?? estado;
  }

  private colorServicio(estado: string): string {
    const map: Record<string, string> = {
      RECIBIDO: '#42A5F5',
      ESPERANDO_EVALUACION_GARANTIA: '#AB47BC',
      PRESUPUESTADO: '#FFA726',
      APROBADO: '#66BB6A',
      EN_REPARACION: '#26C6DA',
      TERMINADO: '#4CAF50',
      RECHAZADO: '#EF5350',
      GARANTIA_SIN_REPARACION: '#78909C',
      GARANTIA_RECHAZADA: '#E53935',
    };
    return map[estado] ?? '#9E9E9E';
  }

  private labelPresupuesto(estado: string): string {
    const map: Record<string, string> = {
      PENDIENTE: 'Pendiente',
      EN_CURSO: 'En Curso',
      LISTO: 'Listo',
      ENVIADO: 'Enviado',
      VENCIDO: 'Vencido',
      APROBADO: 'Aprobado',
      RECHAZADO: 'Rechazado',
    };
    return map[estado] ?? estado;
  }

  private colorPresupuesto(estado: string): string {
    const map: Record<string, string> = {
      PENDIENTE: '#FFC107',
      EN_CURSO: '#42A5F5',
      LISTO: '#66BB6A',
      ENVIADO: '#AB47BC',
      VENCIDO: '#FF7043',
      APROBADO: '#4CAF50',
      RECHAZADO: '#EF5350',
    };
    return map[estado] ?? '#9E9E9E';
  }

  private labelOrden(estado: string): string {
    const map: Record<string, string> = {
      PENDIENTE: 'Pendiente',
      EN_PROGRESO: 'En Progreso',
      TERMINADA: 'Terminada',
      CANCELADA: 'Cancelada',
    };
    return map[estado] ?? estado;
  }

  private colorOrden(estado: string): string {
    const map: Record<string, string> = {
      PENDIENTE: '#FFC107',
      EN_PROGRESO: '#42A5F5',
      TERMINADA: '#4CAF50',
      CANCELADA: '#EF5350',
    };
    return map[estado] ?? '#9E9E9E';
  }
}
