import { Component, OnInit, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { PresupuestoService } from '../../services/presupuesto.service';
import { ServicioService } from '../../services/servicio.service';
import { RepuestoService } from '../../services/repuesto.service';
import { Presupuesto, EstadoPresupuesto, DetallePresupuesto, PresupuestoCreateDto, EnvioPresupuestoDto } from '../../models/presupuesto.model';
import { finalize } from 'rxjs';
import { ServicioResponse } from '../../models/servicio.model';
import { Repuesto } from '../../models/repuesto.model';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { BadgeModule } from 'primeng/badge';
import { DividerModule } from 'primeng/divider';
import { InputTextModule } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { InputNumberModule } from 'primeng/inputnumber';
import { DatePickerModule } from 'primeng/datepicker';
import { TableModule } from 'primeng/table';
import { AutoCompleteModule, AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { DialogModule } from 'primeng/dialog';
import { CheckboxModule } from 'primeng/checkbox';

@Component({
  selector: 'app-presupuesto-detail',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    ButtonModule,
    CardModule,
    ProgressSpinnerModule,
    BadgeModule,
    DividerModule,
    InputTextModule,
    Textarea,
    InputNumberModule,
    DatePickerModule,
    TableModule,
    AutoCompleteModule,
    DialogModule,
    CheckboxModule
  ],
  templateUrl: './presupuesto-detail.html',
  styleUrl: './presupuesto-detail.scss'
})
export class PresupuestoDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly presupuestoService = inject(PresupuestoService);
  private readonly servicioService = inject(ServicioService);
  private readonly repuestoService = inject(RepuestoService);
  private readonly messageService = inject(MessageService);

  readonly presupuesto = signal<Presupuesto | null>(null);
  readonly servicio = signal<ServicioResponse | null>(null);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly guardando = signal<boolean>(false);
  readonly modoCreacion = signal<boolean>(false);

  readonly EstadoPresupuesto = EstadoPresupuesto;

  formularioPresupuesto!: FormGroup;

  // Autocomplete de repuestos
  readonly repuestosFiltrados = signal<Repuesto[]>([]);

  // Dialog de envío
  readonly dialogEnvioVisible = signal<boolean>(false);
  readonly mostrarOriginal = signal<boolean>(true);
  readonly mostrarAlternativo = signal<boolean>(false);
  readonly mensajeAdicional = signal<string>('');
  readonly enviandoEmail = signal<boolean>(false);

  // Computed signals para lógica de edición
  readonly esEditable = computed(() => {
    const p = this.presupuesto();
    if (!p) return this.modoCreacion();
    // Solo se puede editar si está EN_CURSO
    return p.estado === EstadoPresupuesto.EN_CURSO;
  });

  readonly esPendiente = computed(() => {
    const p = this.presupuesto();
    return p?.estado === EstadoPresupuesto.PENDIENTE;
  });

  readonly puedeIniciar = computed(() => {
    const p = this.presupuesto();
    return p?.estado === EstadoPresupuesto.PENDIENTE;
  });

  readonly puedeMarcarListo = computed(() => {
    const p = this.presupuesto();
    return p?.estado === EstadoPresupuesto.EN_CURSO;
  });

  readonly puedeEditar = computed(() => {
    const p = this.presupuesto();
    return p?.estado === EstadoPresupuesto.LISTO;
  });

  readonly puedeEnviar = computed(() => {
    const p = this.presupuesto();
    return p?.estado === EstadoPresupuesto.LISTO;
  });

  readonly puedeCrearOrden = computed(() => {
    const p = this.presupuesto();
    // Puede crear orden si está aprobado y no tiene órdenes de trabajo
    return p?.estado === EstadoPresupuesto.APROBADO;
  });

  readonly soloLectura = computed(() => {
    const p = this.presupuesto();
    if (!p) return false;
    return p.estado === EstadoPresupuesto.PENDIENTE ||
           p.estado === EstadoPresupuesto.LISTO ||
           p.estado === EstadoPresupuesto.ENVIADO ||
           p.estado === EstadoPresupuesto.APROBADO ||
           p.estado === EstadoPresupuesto.RECHAZADO;
  });

  // Métodos para cálculos (no pueden ser computed porque formularios reactivos no son reactivos con signals)
  montoRepuestosOriginalCalculado(): number {
    const detalles = this.getDetallesFormArray();
    if (!detalles || detalles.length === 0) return 0;

    return detalles.controls.reduce((total, control) => {
      const cantidad = control.get('cantidad')?.value || 0;
      const precio = control.get('precioOriginal')?.value || 0;
      return total + (cantidad * precio);
    }, 0);
  }

  montoRepuestosAlternativoCalculado(): number {
    const detalles = this.getDetallesFormArray();
    if (!detalles || detalles.length === 0) return 0;

    let tieneAlternativos = false;
    const montoRepuestos = detalles.controls.reduce((total, control) => {
      const cantidad = control.get('cantidad')?.value || 0;
      const precioAlt = control.get('precioAlternativo')?.value;
      if (precioAlt) {
        tieneAlternativos = true;
        return total + (cantidad * precioAlt);
      }
      return total;
    }, 0);

    return tieneAlternativos ? montoRepuestos : 0;
  }

  montoTotalOriginalCalculado(): number {
    const montoRepuestos = this.montoRepuestosOriginalCalculado();
    const manoObra = this.formularioPresupuesto?.get('manoObra')?.value || 0;
    return montoRepuestos + manoObra;
  }

  montoTotalAlternativoCalculado(): number | null {
    const montoRepuestos = this.montoRepuestosAlternativoCalculado();
    if (montoRepuestos === 0) return null;

    const manoObra = this.formularioPresupuesto?.get('manoObra')?.value || 0;
    return montoRepuestos + manoObra;
  }

  ngOnInit(): void {
    this.inicializarFormulario();
    
    const id = this.route.snapshot.paramMap.get('id');
    const servicioId = this.route.snapshot.queryParamMap.get('servicioId');
    
    if (id && id !== 'nuevo') {
      // Modo visualización/edición
      this.modoCreacion.set(false);
      this.loadPresupuesto(+id);
    } else if (servicioId) {
      // Modo creación desde servicio
      this.modoCreacion.set(true);
      this.loadServicio(+servicioId);
    } else {
      this.error.set('Se requiere un ID de presupuesto o de servicio');
      this.loading.set(false);
    }
  }

  private inicializarFormulario(): void {
    this.formularioPresupuesto = this.fb.group({
      diagnostico: ['', Validators.required],
      manoObra: [0, [Validators.required, Validators.min(0)]],
      fechaVencimiento: [null],
      detalles: this.fb.array([])
    });
  }

  get detalles(): FormArray {
    return this.formularioPresupuesto.get('detalles') as FormArray;
  }

  private getDetallesFormArray(): FormArray {
    return this.formularioPresupuesto?.get('detalles') as FormArray;
  }

  agregarDetalle(): void {
    const detalleForm = this.fb.group({
      item: ['', [Validators.required, Validators.maxLength(200)]],
      cantidad: [1, [Validators.required, Validators.min(1)]],
      precioOriginal: [null, Validators.min(0)],
      precioAlternativo: [null, Validators.min(0)]
    });

    this.detalles.push(detalleForm);
  }

  eliminarDetalle(index: number): void {
    this.detalles.removeAt(index);
  }

  onRepuestoSelect(event: any, index: number): void {
    // Cuando se selecciona un repuesto del autocomplete, extraemos solo la descripción completa
    const detalle = this.detalles.at(index);
    if (event && typeof event === 'object' && event.descripcionCompleta) {
      detalle.patchValue({
        item: event.descripcionCompleta
      });
    }
  }

  calcularSubtotalOriginal(index: number): number {
    const detalle = this.detalles.at(index);
    const cantidad = detalle.get('cantidad')?.value || 0;
    const precio = detalle.get('precioOriginal')?.value || 0;
    return cantidad * precio;
  }

  calcularSubtotalAlternativo(index: number): number | null {
    const detalle = this.detalles.at(index);
    const cantidad = detalle.get('cantidad')?.value || 0;
    const precioAlt = detalle.get('precioAlternativo')?.value;
    return precioAlt ? cantidad * precioAlt : null;
  }

  loadPresupuesto(id: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.presupuestoService.obtenerPresupuestoPorId(id).subscribe({
      next: (presupuesto) => {
        console.log('Presupuesto cargado:', presupuesto);
        console.log('Estado:', presupuesto.estado);
        console.log('Diagnóstico:', presupuesto.diagnostico);
        console.log('Detalles:', presupuesto.detalles);
        console.log('Mano de obra:', presupuesto.manoObra);
        console.log('Montos:', {
          montoRepuestosOriginal: presupuesto.montoRepuestosOriginal,
          montoTotalOriginal: presupuesto.montoTotalOriginal,
          montoRepuestosAlternativo: presupuesto.montoRepuestosAlternativo,
          montoTotalAlternativo: presupuesto.montoTotalAlternativo
        });

        this.presupuesto.set(presupuesto);

        // Si el presupuesto está EN_CURSO, cargar los datos al formulario para edición
        if (presupuesto.estado === EstadoPresupuesto.EN_CURSO) {
          this.cargarDatosAlFormulario(presupuesto);
        }

        // Cargar también el servicio para mostrar el resumen
        if (presupuesto.servicioId) {
          this.loadServicio(presupuesto.servicioId);
        }

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

  private cargarDatosAlFormulario(presupuesto: Presupuesto): void {
    // Cargar diagnóstico y mano de obra
    this.formularioPresupuesto.patchValue({
      diagnostico: presupuesto.diagnostico || '',
      manoObra: presupuesto.manoObra || 0,
      fechaVencimiento: presupuesto.fechaVencimiento ? new Date(presupuesto.fechaVencimiento) : null
    });

    // Limpiar detalles existentes
    this.detalles.clear();

    // Cargar detalles
    if (presupuesto.detalles && presupuesto.detalles.length > 0) {
      presupuesto.detalles.forEach(detalle => {
        const detalleForm = this.fb.group({
          item: [detalle.item, [Validators.required, Validators.maxLength(200)]],
          cantidad: [detalle.cantidad, [Validators.required, Validators.min(1)]],
          precioOriginal: [detalle.precioOriginal, Validators.min(0)],
          precioAlternativo: [detalle.precioAlternativo || null, Validators.min(0)]
        });
        this.detalles.push(detalleForm);
      });
    }
  }

  loadServicio(servicioId: number): void {
    this.servicioService.obtenerServicioPorId(servicioId).subscribe({
      next: (servicio) => {
        this.servicio.set(servicio);
        
        // Si estamos en modo creación, pre-llenar el problema desde el servicio
        if (this.modoCreacion()) {
          this.formularioPresupuesto.patchValue({
            problema: servicio.fallaReportada || ''
          });
        }
        
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading servicio:', err);
        this.error.set('No se pudo cargar el servicio');
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cargar el servicio'
        });
      }
    });
  }

  guardarPresupuesto(): void {
    if (this.formularioPresupuesto.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Por favor, complete todos los campos requeridos'
      });
      return;
    }

    if (this.detalles.length === 0) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Debe agregar al menos un ítem al presupuesto'
      });
      return;
    }

    this.guardando.set(true);
    const formValues = this.formularioPresupuesto.value;
    const servicioActual = this.servicio();
    
    if (!servicioActual) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se encontró el servicio asociado'
      });
      this.guardando.set(false);
      return;
    }

    // Obtener el empleado ID del usuario actual (esto puede venir de un servicio de autenticación)
    // Por ahora usamos un ID por defecto ya que el servicio solo tiene el nombre del empleado
    const empleadoId = 1; // TODO: obtener del usuario logueado o del servicio si se agrega empleadoRecepcionId

    const presupuestoDto: PresupuestoCreateDto = {
      servicioId: servicioActual.id,
      empleadoId: empleadoId,
      diagnostico: formValues.diagnostico,
      detalles: formValues.detalles.map((d: any) => ({
        item: typeof d.item === 'string' ? d.item : (d.item?.descripcionCompleta || d.item),
        cantidad: d.cantidad,
        precioOriginal: d.precioOriginal,
        precioAlternativo: d.precioAlternativo || undefined
      })),
      manoObra: formValues.manoObra,
      fechaVencimiento: formValues.fechaVencimiento
        ? new Date(formValues.fechaVencimiento).toISOString().split('T')[0]
        : undefined,
      estado: EstadoPresupuesto.EN_CURSO // Se crea directamente en EN_CURSO para que el técnico pueda trabajar
    };

    this.presupuestoService.crearPresupuesto(presupuestoDto).subscribe({
      next: (presupuesto) => {
        this.guardando.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Presupuesto creado correctamente'
        });
        this.router.navigate(['/presupuestos', presupuesto.id]);
      },
      error: (err) => {
        console.error('Error al crear presupuesto:', err);
        this.guardando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo crear el presupuesto'
        });
      }
    });
  }

  goBack(): void {
    if (this.modoCreacion()) {
      const servicioActual = this.servicio();
      if (servicioActual) {
        this.router.navigate(['/servicios', servicioActual.id]);
      } else {
        this.router.navigate(['/servicios']);
      }
    } else {
      this.router.navigate(['/presupuestos']);
    }
  }

  verServicio(): void {
    const servicioId = this.presupuesto()?.servicioId || this.servicio()?.id;
    if (servicioId) {
      this.router.navigate(['/servicios', servicioId]);
    }
  }

  crearOrdenDeTrabajo(): void {
    const presupuestoId = this.presupuesto()?.id;
    if (!presupuestoId) return;

    this.guardando.set(true);

    this.presupuestoService.crearOrdenDeTrabajo(presupuestoId)
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe({
        next: (response) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: response.message
          });
          // Navegar a la orden de trabajo creada
          this.router.navigate(['/ordenes-trabajo', response.ordenTrabajoId]);
        },
        error: (err) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: err.error?.error || 'No se pudo crear la orden de trabajo'
          });
        }
      });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-UY', {
      style: 'currency',
      currency: 'UYU'
    }).format(amount);
  }

  formatFecha(fecha: string | Date | undefined): string {
    if (!fecha) return 'N/A';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-UY', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  tieneMontoAlternativo(): boolean {
    const p = this.presupuesto();
    return p !== null &&
           p.montoTotalAlternativo !== null &&
           p.montoTotalAlternativo !== undefined &&
           p.montoTotalAlternativo > 0;
  }

  getEstadoBadgeSeverity(estado: EstadoPresupuesto): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    switch (estado) {
      case EstadoPresupuesto.PENDIENTE:
        return 'warn';
      case EstadoPresupuesto.EN_CURSO:
        return 'info';
      case EstadoPresupuesto.LISTO:
        return 'secondary';
      case EstadoPresupuesto.ENVIADO:
        return 'contrast';
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
      case EstadoPresupuesto.LISTO:
        return 'Listo';
      case EstadoPresupuesto.ENVIADO:
        return 'Enviado';
      case EstadoPresupuesto.APROBADO:
        return 'Aprobado';
      case EstadoPresupuesto.RECHAZADO:
        return 'Rechazado';
      default:
        return estado;
    }
  }

  iniciarPresupuesto(): void {
    const p = this.presupuesto();
    if (!p) return;

    this.guardando.set(true);

    // TODO: Obtener el empleadoId del usuario logueado
    // Por ahora usamos un ID por defecto
    const empleadoId = 1;

    this.presupuestoService.cambiarEstado(p.id, EstadoPresupuesto.EN_CURSO).subscribe({
      next: () => {
        this.guardando.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Presupuesto iniciado. Ahora puede editarlo.'
        });
        this.loadPresupuesto(p.id);
      },
      error: (err: any) => {
        console.error('Error:', err);
        this.guardando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo iniciar el presupuesto'
        });
      }
    });
  }

  marcarComoListo(): void {
    const p = this.presupuesto();
    if (!p) return;

    if (this.formularioPresupuesto.invalid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Por favor, complete todos los campos requeridos antes de marcar como listo'
      });
      return;
    }

    if (this.detalles.length === 0) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Debe agregar al menos un ítem al presupuesto'
      });
      return;
    }

    this.guardando.set(true);

    // Primero actualizar los datos del presupuesto
    const formValues = this.formularioPresupuesto.value;
    const presupuestoDto = {
      diagnostico: formValues.diagnostico,
      detalles: formValues.detalles.map((d: any) => ({
        item: typeof d.item === 'string' ? d.item : (d.item?.descripcionCompleta || d.item),
        cantidad: d.cantidad,
        precioOriginal: d.precioOriginal,
        precioAlternativo: d.precioAlternativo || undefined
      })),
      manoObra: formValues.manoObra,
      fechaVencimiento: formValues.fechaVencimiento
        ? new Date(formValues.fechaVencimiento).toISOString().split('T')[0]
        : undefined
    };

    // Actualizar primero, luego cambiar estado
    this.presupuestoService.actualizarPresupuesto(p.id, presupuestoDto).subscribe({
      next: () => {
        // Ahora cambiar el estado a LISTO
        this.presupuestoService.cambiarEstado(p.id, EstadoPresupuesto.LISTO).subscribe({
          next: () => {
            this.guardando.set(false);
            this.messageService.add({
              severity: 'success',
              summary: 'Éxito',
              detail: 'Presupuesto marcado como listo para revisión'
            });
            this.loadPresupuesto(p.id);
          },
          error: (err: any) => {
            console.error('Error al cambiar estado:', err);
            this.guardando.set(false);
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No se pudo marcar el presupuesto como listo'
            });
          }
        });
      },
      error: (err: any) => {
        console.error('Error al actualizar presupuesto:', err);
        this.guardando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo actualizar el presupuesto'
        });
      }
    });
  }

  volverAEditar(): void {
    const p = this.presupuesto();
    if (!p) return;

    this.guardando.set(true);

    this.presupuestoService.cambiarEstado(p.id, EstadoPresupuesto.EN_CURSO).subscribe({
      next: () => {
        this.guardando.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Presupuesto vuelto a estado En Curso'
        });
        this.loadPresupuesto(p.id);
      },
      error: (err: any) => {
        console.error('Error:', err);
        this.guardando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo cambiar el estado del presupuesto'
        });
      }
    });
  }

  buscarRepuestos(event: AutoCompleteCompleteEvent): void {
    const query = event.query;
    this.repuestoService.buscar(query).subscribe({
      next: (repuestos) => {
        this.repuestosFiltrados.set(repuestos);
      },
      error: (err) => {
        console.error('Error al buscar repuestos:', err);
        this.repuestosFiltrados.set([]);
      }
    });
  }

  abrirDialogoEnvio(): void {
    const p = this.presupuesto();
    if (!p) return;

    // Inicializar los valores del diálogo con los valores actuales del presupuesto
    this.mostrarOriginal.set(p.mostrarOriginal);
    this.mostrarAlternativo.set(p.mostrarAlternativo);
    this.mensajeAdicional.set('');
    this.dialogEnvioVisible.set(true);
  }

  cerrarDialogoEnvio(): void {
    this.dialogEnvioVisible.set(false);
  }

  confirmarEnvio(): void {
    const p = this.presupuesto();
    if (!p) return;

    // Validar que al menos una opción esté seleccionada
    if (!this.mostrarOriginal() && !this.mostrarAlternativo()) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Debe seleccionar al menos un precio para mostrar'
      });
      return;
    }

    this.enviandoEmail.set(true);

    const dto: EnvioPresupuestoDto = {
      presupuestoId: p.id,
      mostrarOriginal: this.mostrarOriginal(),
      mostrarAlternativo: this.mostrarAlternativo(),
      mensajeAdicional: this.mensajeAdicional() || undefined
    };

    this.presupuestoService.enviarPresupuestoACliente(dto)
      .pipe(finalize(() => this.enviandoEmail.set(false)))
      .subscribe({
        next: (response) => {
          this.presupuesto.set(response);
          this.messageService.add({
            severity: 'success',
            summary: 'Éxito',
            detail: 'Presupuesto enviado al cliente por email'
          });
          this.cerrarDialogoEnvio();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: error.error?.message || 'Error al enviar el presupuesto'
          });
        }
      });
  }
}
