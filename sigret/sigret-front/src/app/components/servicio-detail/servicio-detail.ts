import { Component, OnInit, AfterViewInit, signal, inject, ChangeDetectorRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ServicioService } from '../../services/servicio.service';
import { PresupuestoService } from '../../services/presupuesto.service';
import { OrdenTrabajoService } from '../../services/orden-trabajo.service';
import { ServicioResponse, EstadoServicio } from '../../models/servicio.model';
import { ServicioUpdateDto } from '../../models/servicio-update.dto';
import { Presupuesto } from '../../models/presupuesto.model';
import { OrdenTrabajo } from '../../models/orden-trabajo.model';
import { ItemServicioOriginal } from '../../models/item-evaluacion-garantia.model';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { Button, ButtonModule } from 'primeng/button';
import { Card } from 'primeng/card';
import { Tag } from 'primeng/tag';
import { Divider } from 'primeng/divider';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Tabs, TabList, Tab, TabPanels, TabPanel } from 'primeng/tabs';
import { InputNumber } from 'primeng/inputnumber';
import { DatePicker } from 'primeng/datepicker';
import { Select } from 'primeng/select';
import { Checkbox } from 'primeng/checkbox';
import { RadioButton } from 'primeng/radiobutton';
import { TextareaModule } from 'primeng/textarea';
import { Dialog } from 'primeng/dialog';
import { AuthService } from '../../services/auth.service';
import { FinalizarTrabajoDialogComponent } from '../finalizar-trabajo-dialog/finalizar-trabajo-dialog';

@Component({
  selector: 'app-servicio-detail',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    Button,
    ButtonModule,
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
    Checkbox,
    RadioButton,
    TextareaModule,
    Dialog,
    ConfirmDialog,
    FinalizarTrabajoDialogComponent
  ],
  providers: [ConfirmationService],
  templateUrl: './servicio-detail.html',
  styleUrl: './servicio-detail.scss'
})
export class ServicioDetail implements OnInit, AfterViewInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly servicioService = inject(ServicioService);
  private readonly presupuestoService = inject(PresupuestoService);
  private readonly ordenTrabajoService = inject(OrdenTrabajoService);
  private readonly messageService = inject(MessageService);
  private readonly authService = inject(AuthService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly cdr = inject(ChangeDetectorRef);

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
  readonly eliminando = signal<boolean>(false);

  // Dialog de finalizar trabajo
  @ViewChild(FinalizarTrabajoDialogComponent) finalizarDialog!: FinalizarTrabajoDialogComponent;

  // Variables para modal de evaluación de garantía
  mostrarModalEvaluacion = false;
  guardandoEvaluacion = false;
  resultadoEvaluacion: 'CUMPLE' | 'NO_CUMPLE' | 'SIN_REPARACION' = 'CUMPLE';
  evaluacionObservaciones = '';

  // Signals para items de la orden de trabajo original
  readonly cargandoItems = signal<boolean>(false);
  readonly itemsEvaluacion = signal<Array<{
    repuestoId: number | null;
    item: string;
    cantidad: number;
    comentario: string;
    seleccionado: boolean;
    comentarioEvaluacion: string;
  }>>([]);

  // FormGroup
  formularioEdicion!: FormGroup;

  // Opciones para selects
  readonly estadosOptions = [
    { label: 'Recibido', value: 'RECIBIDO' },
    { label: 'Presupuestado', value: 'PRESUPUESTADO' },
    { label: 'Aprobado', value: 'APROBADO' },
    { label: 'En Reparación', value: 'EN_REPARACION' },
    { label: 'Terminado', value: 'TERMINADO' },
    { label: 'Rechazado', value: 'RECHAZADO' },
    { label: 'Finalizado', value: 'FINALIZADO' }
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

  ngAfterViewInit(): void {
    // Forzar repintado para resolver problemas de estilos PrimeFlex
    setTimeout(() => {
      this.cdr.detectChanges();
    }, 0);
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
        console.log('Servicio cargado:', {
          id: servicio.id,
          numeroServicio: servicio.numeroServicio,
          estado: servicio.estado,
          esGarantia: servicio.esGarantia,
          servicioGarantiaId: servicio.servicioGarantiaId
        });
        this.servicio.set(servicio);
        this.loading.set(false);

        // Si es una garantía, cargar el servicio original
        if (servicio.esGarantia && servicio.servicioGarantiaId) {
          this.cargarServicioOriginal(servicio.servicioGarantiaId);
        }

        // Forzar repintado después de cargar el servicio
        setTimeout(() => {
          this.cdr.detectChanges();
        }, 0);
      },
      error: (err) => {
        console.error('Error al cargar servicio:', err);
        this.error.set('Error al cargar el servicio. Por favor, intente nuevamente.');
        this.loading.set(false);
        this.cdr.markForCheck();
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
        // Forzar repintado después de cargar servicio original
        setTimeout(() => {
          this.cdr.detectChanges();
        }, 0);
      },
      error: (err) => {
        console.error('Error al cargar servicio original:', err);
        this.loadingServicioOriginal.set(false);
        this.cdr.markForCheck();
      }
    });
  }

  private cargarPresupuestos(servicioId: number): void {
    this.loadingPresupuestos.set(true);

    this.presupuestoService.obtenerPresupuestosPorServicio(servicioId).subscribe({
      next: (presupuestos) => {
        this.presupuestos.set(presupuestos);
        this.loadingPresupuestos.set(false);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error al cargar presupuestos:', err);
        this.loadingPresupuestos.set(false);
        this.cdr.markForCheck();
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
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error al cargar órdenes de trabajo:', err);
        this.loadingOrdenes.set(false);
        this.cdr.markForCheck();
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
        this.cdr.markForCheck();
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Servicio actualizado correctamente'
        });
      },
      error: (err) => {
        console.error('Error al actualizar servicio:', err);
        this.guardando.set(false);
        this.cdr.markForCheck();
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo actualizar el servicio'
        });
      }
    });
  }

  volver(): void {
    const servicio = this.servicio();
    // Si es una garantía, volver al tablero de garantías
    if (servicio?.esGarantia) {
      this.router.navigate(['/garantias']);
    } else {
      this.router.navigate(['/servicios']);
    }
  }

  confirmarEliminacion(): void {
    const servicio = this.servicio();
    if (!servicio) return;

    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar el servicio ${servicio.numeroServicio}? Se desactivará junto con todos sus presupuestos y órdenes de trabajo asociados.`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.eliminarServicio()
    });
  }

  private eliminarServicio(): void {
    const servicio = this.servicio();
    if (!servicio) return;

    this.eliminando.set(true);
    this.servicioService.eliminarServicio(servicio.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Servicio Eliminado',
          detail: `El servicio ${servicio.numeroServicio} fue eliminado correctamente`
        });
        this.router.navigate(['/servicios']);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.error?.message || 'Error al eliminar el servicio'
        });
        this.eliminando.set(false);
      }
    });
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
      'CANCELADA': 'danger',
      'FINALIZADO': 'success'
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

  getMontoConfirmado(presupuesto: Presupuesto): number {
    // Si el presupuesto está aprobado y tiene un tipo confirmado, mostrar ese monto
    if (presupuesto.tipoConfirmado === 'ORIGINAL') {
      return presupuesto.montoTotalOriginal;
    } else if (presupuesto.tipoConfirmado === 'ALTERNATIVO' && presupuesto.montoTotalAlternativo) {
      return presupuesto.montoTotalAlternativo;
    }
    // Por defecto, mostrar el monto original
    return presupuesto.montoTotalOriginal;
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
      // Navegar y luego recargar el componente
      this.router.navigate(['/servicios', servicioOriginal.id]).then(() => {
        // Recargar todos los datos del nuevo servicio
        this.cargarServicio(servicioOriginal.id);
        this.cargarPresupuestos(servicioOriginal.id);
        this.cargarOrdenesTrabajo(servicioOriginal.id);
      });
    }
  }

  // Métodos para evaluación de garantía
  activarModoEdicionEvaluacion(): void {
    console.log('🔵 Activando modo edición de evaluación');
    const servicio = this.servicio();
    if (servicio) {
      // Determinar el resultado basado en el estado actual
      if (servicio.estado === 'GARANTIA_RECHAZADA') {
        this.resultadoEvaluacion = 'NO_CUMPLE';
      } else if (servicio.estado === 'GARANTIA_SIN_REPARACION') {
        this.resultadoEvaluacion = 'SIN_REPARACION';
      } else if (servicio.garantiaCumpleCondiciones) {
        this.resultadoEvaluacion = 'CUMPLE';
      } else {
        this.resultadoEvaluacion = 'CUMPLE'; // Default
      }
      this.evaluacionObservaciones = servicio.observacionesEvaluacionGarantia || '';

      // Cargar items del servicio original si cumple garantía
      if (this.resultadoEvaluacion === 'CUMPLE') {
        console.log('✅ Garantía CUMPLE - Cargando items del servicio original');
        this.cargarItemsServicioOriginal();
      }
    } else {
      this.resultadoEvaluacion = 'CUMPLE';
      this.evaluacionObservaciones = '';
    }
    this.mostrarModalEvaluacion = true;
  }

  private cargarItemsServicioOriginal(): void {
    const servicio = this.servicio();
    if (!servicio || !servicio.esGarantia || !servicio.servicioGarantiaId) {
      console.warn('⚠️ No hay servicio original para cargar items');
      return;
    }

    console.log('📦 Cargando items del servicio de garantía ID:', servicio.id, 'servicio original ID:', servicio.servicioGarantiaId);
    this.cargandoItems.set(true);

    // Pasamos el ID del servicio de garantía actual (no el ID del servicio original)
    this.servicioService.obtenerItemsServicioOriginal(servicio.id).subscribe({
      next: (items) => {
        console.log('✅ Items cargados:', items);
        // Mapear los items a la estructura esperada
        const itemsMapeados = items.map(item => ({
          repuestoId: item.repuestoId,
          item: item.item,
          cantidad: item.cantidad,
          comentario: item.comentario || '',
          seleccionado: false,
          comentarioEvaluacion: ''
        }));
        this.itemsEvaluacion.set(itemsMapeados);
        this.cargandoItems.set(false);
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error('❌ Error al cargar items:', error);
        this.cargandoItems.set(false);
        this.cdr.markForCheck();
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los items de la orden de trabajo original'
        });
      }
    });
  }

  cerrarModalEvaluacion(): void {
    this.mostrarModalEvaluacion = false;
  }

  guardarEvaluacion(): void {
    const servicio = this.servicio();
    if (!servicio) return;

    // Validar que se hayan ingresado observaciones
    if (!this.evaluacionObservaciones || this.evaluacionObservaciones.trim() === '') {
      this.messageService.add({
        severity: 'warn',
        summary: 'Campo requerido',
        detail: 'Debe ingresar observaciones de la evaluación'
      });
      return;
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser || !currentUser.empleadoId) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo identificar al empleado logueado'
      });
      return;
    }

    this.guardandoEvaluacion = true;

    // Determinar el nuevo estado según el resultado de la evaluación
    let nuevoEstado: EstadoServicio;
    let garantiaCumple: boolean;

    switch (this.resultadoEvaluacion) {
      case 'CUMPLE':
        nuevoEstado = EstadoServicio.EN_REPARACION;
        garantiaCumple = true;
        break;
      case 'NO_CUMPLE':
        nuevoEstado = EstadoServicio.GARANTIA_RECHAZADA;
        garantiaCumple = false;
        break;
      case 'SIN_REPARACION':
        nuevoEstado = EstadoServicio.GARANTIA_SIN_REPARACION;
        garantiaCumple = true; // Cumple pero no necesita reparación
        break;
    }

    // Obtener items seleccionados (solo los que tienen repuestoId)
    const itemsSeleccionados = this.itemsEvaluacion()
      .filter(item => item.seleccionado && item.repuestoId !== null)
      .map(item => ({
        repuestoId: item.repuestoId!,
        comentario: item.comentarioEvaluacion || item.comentario
      }));

    const updateDto: ServicioUpdateDto = {
      estado: nuevoEstado,
      tecnicoEvaluacionId: currentUser.empleadoId,
      garantiaCumpleCondiciones: garantiaCumple,
      observacionesEvaluacionGarantia: this.evaluacionObservaciones || undefined,
      itemsEvaluacionGarantia: itemsSeleccionados.length > 0 ? itemsSeleccionados : undefined
    };

    this.servicioService.actualizarServicio(servicio.id, updateDto).subscribe({
      next: (servicioActualizado) => {
        console.log('Servicio actualizado recibido del backend:', servicioActualizado);

        // Si la garantía cumple, crear la orden de trabajo con los items seleccionados
        if (this.resultadoEvaluacion === 'CUMPLE') {
          console.log('✅ Creando orden de trabajo para garantía con items seleccionados');
          this.ordenTrabajoService.crearOrdenTrabajoGarantia(
            servicio.id,
            currentUser.empleadoId,
            this.evaluacionObservaciones,
            itemsSeleccionados
          ).subscribe({
            next: (ordenCreada) => {
              console.log('✅ Orden de trabajo creada:', ordenCreada);
              this.guardandoEvaluacion = false;
              this.mostrarModalEvaluacion = false;

              // Recargar el servicio completo
              this.cargarServicio(servicio.id);
              this.cargarOrdenesTrabajo(servicio.id);

              this.messageService.add({
                severity: 'success',
                summary: 'Evaluación registrada',
                detail: 'Garantía aceptada y orden de trabajo creada'
              });
            },
            error: (error) => {
              console.error('❌ Error al crear orden de trabajo:', error);
              this.guardandoEvaluacion = false;
              this.messageService.add({
                severity: 'error',
                summary: 'Error',
                detail: 'La evaluación se guardó pero hubo un error al crear la orden de trabajo'
              });
            }
          });
        } else {
          this.guardandoEvaluacion = false;
          this.mostrarModalEvaluacion = false;

          // Recargar el servicio completo
          this.cargarServicio(servicio.id);

          this.messageService.add({
            severity: 'success',
            summary: 'Evaluación registrada',
            detail: `Garantía evaluada y movida a ${this.getEstadoLabel(nuevoEstado)}`
          });
        }
      },
      error: (error) => {
        console.error('Error al registrar evaluación:', error);
        this.guardandoEvaluacion = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo registrar la evaluación'
        });
      }
    });
  }

  // Métodos para finalizar trabajo
  abrirDialogFinalizar(): void {
    this.finalizarDialog.showDialog();
  }

  onServicioFinalizado(): void {
    const servicio = this.servicio();
    if (servicio) {
      this.cargarServicio(servicio.id);
    }
  }

  private getEstadoLabel(estado: string): string {
    const labels: Record<string, string> = {
      'EN_REPARACION': 'En Reparación',
      'GARANTIA_RECHAZADA': 'Garantía Rechazada',
      'GARANTIA_SIN_REPARACION': 'Sin Reparación'
    };
    return labels[estado] || estado;
  }
}
