import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ServicioService } from '../../services/servicio.service';
import { PresupuestoService } from '../../services/presupuesto.service';
import { OrdenTrabajoService } from '../../services/orden-trabajo.service';
import { ServicioResponse, EstadoServicio } from '../../models/servicio.model';
import { ServicioUpdateDto } from '../../models/servicio-update.dto';
import { Presupuesto } from '../../models/presupuesto.model';
import { OrdenTrabajo } from '../../models/orden-trabajo.model';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { Tag } from 'primeng/tag';
import { Divider } from 'primeng/divider';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Tabs, TabList, Tab, TabPanels, TabPanel } from 'primeng/tabs';
import { InputNumber } from 'primeng/inputnumber';
import { DatePicker } from 'primeng/datepicker';
import { Select } from 'primeng/select';
import { Checkbox } from 'primeng/checkbox';

@Component({
  selector: 'app-servicio-detail',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Button,
    Card,
    Tag,
    Divider,
    ProgressSpinner,
    Tabs,
    TabList,
    Tab,
    TabPanels,
    TabPanel,
    InputNumber,
    DatePicker,
    Select,
    Checkbox
  ],
  templateUrl: './servicio-detail.html',
  styleUrl: './servicio-detail.scss'
})
export class ServicioDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly servicioService = inject(ServicioService);
  private readonly presupuestoService = inject(PresupuestoService);
  private readonly ordenTrabajoService = inject(OrdenTrabajoService);
  private readonly messageService = inject(MessageService);

  // Signals
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly servicio = signal<ServicioResponse | null>(null);
  readonly servicioOriginal = signal<ServicioResponse | null>(null);
  readonly loadingServicioOriginal = signal<boolean>(false);
  readonly presupuestos = signal<Presupuesto[]>([]);
  readonly ordenesTrabajo = signal<OrdenTrabajo[]>([]);
  readonly loadingPresupuestos = signal<boolean>(true);
  readonly loadingOrdenes = signal<boolean>(true);

  // Signals para modo edición
  readonly modoEdicion = signal<boolean>(false);
  readonly guardando = signal<boolean>(false);

  // FormGroup
  formularioEdicion!: FormGroup;

  // Opciones para selects
  readonly estadosOptions = [
    { label: 'Recibido', value: 'RECIBIDO' },
    { label: 'Presupuestado', value: 'PRESUPUESTADO' },
    { label: 'Aprobado', value: 'APROBADO' },
    { label: 'En Reparación', value: 'EN_REPARACION' },
    { label: 'Terminado', value: 'TERMINADO' },
    { label: 'Rechazado', value: 'RECHAZADO' }
  ];

  readonly tiposIngresoOptions = [
    { label: 'Cliente Trae Equipo', value: 'CLIENTE_TRAE' },
    { label: 'Empresa Busca Equipo', value: 'EMPRESA_BUSCA' }
  ];

  ngOnInit(): void {
    this.inicializarFormulario();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.cargarServicio(+id);
      this.cargarPresupuestos(+id);
      this.cargarOrdenesTrabajo(+id);
    } else {
      this.error.set('ID de servicio no válido');
      this.loading.set(false);
    }
  }

  private inicializarFormulario(): void {
    this.formularioEdicion = this.fb.group({
      // Estado y fechas
      estado: [''],
      fechaDevolucionPrevista: [null],
      fechaDevolucionReal: [null],

      // Pago
      abonaVisita: [false],
      montoVisita: [0, [Validators.min(0)]],
      montoPagado: [0, [Validators.min(0)]],

      // Tipo de ingreso
      tipoIngreso: [''],

      // Firmas
      firmaIngreso: [''],
      firmaConformidad: [''],

      // Garantía
      esGarantia: [false],
      servicioGarantiaId: [null],
      garantiaDentroPlazo: [false],
      garantiaCumpleCondiciones: [false],
      observacionesGarantia: [''],
      tecnicoEvaluacionId: [null],
      observacionesEvaluacionGarantia: ['']
    });
  }

  private cargarServicio(id: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.servicioService.obtenerServicioPorId(id).subscribe({
      next: (servicio) => {
        this.servicio.set(servicio);
        this.loading.set(false);

        // Si es una garantía, cargar el servicio original
        if (servicio.esGarantia && servicio.servicioGarantiaId) {
          this.cargarServicioOriginal(servicio.servicioGarantiaId);
        }
      },
      error: (err) => {
        console.error('Error al cargar servicio:', err);
        this.error.set('Error al cargar el servicio. Por favor, intente nuevamente.');
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cargar el servicio'
        });
      }
    });
  }

  private cargarServicioOriginal(id: number): void {
    this.loadingServicioOriginal.set(true);

    this.servicioService.obtenerServicioPorId(id).subscribe({
      next: (servicioOriginal) => {
        this.servicioOriginal.set(servicioOriginal);
        this.loadingServicioOriginal.set(false);
      },
      error: (err) => {
        console.error('Error al cargar servicio original:', err);
        this.loadingServicioOriginal.set(false);
      }
    });
  }

  private cargarPresupuestos(servicioId: number): void {
    this.loadingPresupuestos.set(true);

    this.presupuestoService.obtenerPresupuestosPorServicio(servicioId).subscribe({
      next: (presupuestos) => {
        this.presupuestos.set(presupuestos);
        this.loadingPresupuestos.set(false);
      },
      error: (err) => {
        console.error('Error al cargar presupuestos:', err);
        this.loadingPresupuestos.set(false);
        this.messageService.add({
          severity: 'warn',
          summary: 'Advertencia',
          detail: 'No se pudieron cargar los presupuestos'
        });
      }
    });
  }

  private cargarOrdenesTrabajo(servicioId: number): void {
    this.loadingOrdenes.set(true);

    this.ordenTrabajoService.obtenerOrdenesTrabajosPorServicio(servicioId).subscribe({
      next: (ordenes) => {
        this.ordenesTrabajo.set(ordenes);
        this.loadingOrdenes.set(false);
      },
      error: (err) => {
        console.error('Error al cargar órdenes de trabajo:', err);
        this.loadingOrdenes.set(false);
        this.messageService.add({
          severity: 'warn',
          summary: 'Advertencia',
          detail: 'No se pudieron cargar las órdenes de trabajo'
        });
      }
    });
  }

  activarModoEdicion(): void {
    const servicio = this.servicio();
    if (!servicio) return;

    // Convertir fechas de string a Date para el DatePicker
    const fechaDevolucionPrevista = servicio.fechaDevolucionPrevista
      ? new Date(servicio.fechaDevolucionPrevista)
      : null;
    const fechaDevolucionReal = servicio.fechaDevolucionReal
      ? new Date(servicio.fechaDevolucionReal)
      : null;

    this.formularioEdicion.patchValue({
      estado: servicio.estado,
      fechaDevolucionPrevista,
      fechaDevolucionReal,
      abonaVisita: servicio.abonaVisita,
      montoVisita: servicio.montoVisita,
      montoPagado: servicio.montoPagado,
      tipoIngreso: servicio.tipoIngreso,
      firmaIngreso: servicio.firmaIngreso || '',
      firmaConformidad: '',
      esGarantia: servicio.esGarantia,
      servicioGarantiaId: null,
      garantiaDentroPlazo: false,
      garantiaCumpleCondiciones: false,
      observacionesGarantia: '',
      tecnicoEvaluacionId: null,
      observacionesEvaluacionGarantia: ''
    });

    this.modoEdicion.set(true);
  }

  cancelarEdicion(): void {
    this.modoEdicion.set(false);
    // No resetear el formulario, solo desactivar el modo edición
    // Los valores se mantendrán hasta que se active nuevamente la edición
  }

  guardarCambios(): void {
    if (this.formularioEdicion.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Por favor, corrija los errores en el formulario'
      });
      return;
    }

    const servicio = this.servicio();
    if (!servicio) return;

    this.guardando.set(true);

    const formValues = this.formularioEdicion.value;

    // Convertir fechas de Date a string ISO para el backend
    // IMPORTANTE: Solo convertir si el valor existe y es una fecha válida
    let fechaDevolucionPrevistaStr: string | undefined = undefined;
    if (formValues.fechaDevolucionPrevista) {
      const fecha = new Date(formValues.fechaDevolucionPrevista);
      if (!isNaN(fecha.getTime())) {
        fechaDevolucionPrevistaStr = fecha.toISOString().split('T')[0];
      }
    }

    let fechaDevolucionRealStr: string | undefined = undefined;
    if (formValues.fechaDevolucionReal) {
      const fecha = new Date(formValues.fechaDevolucionReal);
      if (!isNaN(fecha.getTime())) {
        fechaDevolucionRealStr = fecha.toISOString().split('T')[0];
      }
    }

    const servicioUpdate: ServicioUpdateDto = {
      estado: formValues.estado,
      fechaDevolucionPrevista: fechaDevolucionPrevistaStr,
      fechaDevolucionReal: fechaDevolucionRealStr,
      abonaVisita: formValues.abonaVisita,
      montoVisita: formValues.montoVisita,
      montoPagado: formValues.montoPagado,
      tipoIngreso: formValues.tipoIngreso,
      firmaIngreso: formValues.firmaIngreso || undefined,
      firmaConformidad: formValues.firmaConformidad || undefined,
      esGarantia: formValues.esGarantia,
      servicioGarantiaId: formValues.servicioGarantiaId || undefined,
      garantiaDentroPlazo: formValues.garantiaDentroPlazo,
      garantiaCumpleCondiciones: formValues.garantiaCumpleCondiciones,
      observacionesGarantia: formValues.observacionesGarantia || undefined,
      tecnicoEvaluacionId: formValues.tecnicoEvaluacionId || undefined,
      observacionesEvaluacionGarantia: formValues.observacionesEvaluacionGarantia || undefined
    };

    this.servicioService.actualizarServicio(servicio.id, servicioUpdate).subscribe({
      next: (servicioActualizado) => {
        this.servicio.set(servicioActualizado);
        this.guardando.set(false);
        this.modoEdicion.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Servicio actualizado correctamente'
        });
      },
      error: (err) => {
        console.error('Error al actualizar servicio:', err);
        this.guardando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo actualizar el servicio'
        });
      }
    });
  }

  volver(): void {
    this.router.navigate(['/servicios']);
  }

  getEstadoSeverity(estado: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    const severityMap: Record<string, 'success' | 'info' | 'warn' | 'danger' | 'secondary'> = {
      'RECIBIDO': 'info',
      'PRESUPUESTADO': 'warn',
      'APROBADO': 'info',
      'EN_REPARACION': 'info',
      'TERMINADO': 'success',
      'RECHAZADO': 'danger',
      'PENDIENTE': 'warn',
      'EN_PROGRESO': 'info',
      'EN_CURSO': 'info',
      'TERMINADA': 'success',
      'CANCELADA': 'danger'
    };
    return severityMap[estado] || 'secondary';
  }

  formatFecha(fecha: string | Date | undefined): string {
    if (!fecha) return 'N/A';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatMonto(monto: number | undefined): string {
    if (monto === undefined || monto === null) return '$0';
    return `$${monto.toFixed(2)}`;
  }

  verPresupuesto(presupuestoId: number): void {
    this.router.navigate(['/presupuestos', presupuestoId]);
  }

  verOrdenTrabajo(ordenId: number): void {
    this.router.navigate(['/ordenes-trabajo', ordenId]);
  }

  verServicioOriginal(): void {
    const servicioOriginal = this.servicioOriginal();
    if (servicioOriginal) {
      this.router.navigate(['/servicios', servicioOriginal.id]);
    }
  }
}
