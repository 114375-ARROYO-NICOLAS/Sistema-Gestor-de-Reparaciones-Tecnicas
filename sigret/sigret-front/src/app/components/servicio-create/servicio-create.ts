import { Component, OnInit, signal, inject, ChangeDetectionStrategy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { AutoComplete } from 'primeng/autocomplete';
import { DatePicker } from 'primeng/datepicker';
import { Stepper, StepList, StepPanels, StepPanel, Step } from 'primeng/stepper';
import { Badge } from 'primeng/badge';
import { Toast } from 'primeng/toast';
import { Message } from 'primeng/message';
import { MessageService } from 'primeng/api';
import SignaturePad from 'signature_pad';

import { ServicioService } from '../../services/servicio.service';
import { ClientService } from '../../services/client.service';
import { EmployeeService } from '../../services/employee.service';
import { EquipoService } from '../../services/equipo.service';
import { ServicioCreateDto, DetalleServicio, ServicioList } from '../../models/servicio.model';
import { ClientListDto } from '../../models/client.model';
import { EmployeeListDto } from '../../models/employee.model';
import { EquipoListDto } from '../../models/equipo.model';

@Component({
  selector: 'app-servicio-create',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    Button,
    InputText,
    Textarea,
    InputNumber,
    Select,
    AutoComplete,
    DatePicker,
    Stepper,
    StepList,
    StepPanels,
    StepPanel,
    Step,
    Badge,
    Toast,
    Message
  ],
  templateUrl: './servicio-create.html',
  styleUrl: './servicio-create.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [MessageService]
})
export class ServicioCreateComponent implements OnInit, AfterViewInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);
  private readonly servicioService = inject(ServicioService);
  private readonly clientService = inject(ClientService);
  private readonly employeeService = inject(EmployeeService);
  private readonly equipoService = inject(EquipoService);

  // Canvas para la firma
  @ViewChild('signatureCanvas') signatureCanvas!: ElementRef<HTMLCanvasElement>;
  private signaturePad!: SignaturePad;

  // Signals
  readonly clientes = signal<ClientListDto[]>([]);
  readonly equipos = signal<EquipoListDto[]>([]);
  readonly empleados = signal<EmployeeListDto[]>([]);
  readonly serviciosPrevios = signal<ServicioList[]>([]);
  readonly isSaving = signal(false);
  readonly currentStep = signal(0);
  activeStep = 1; // Para el binding bidireccional con p-stepper

  // Control separado para el autocomplete de cliente (guarda el objeto completo)
  selectedCliente = signal<ClientListDto | null>(null);
  selectedServicioPrevio = signal<ServicioList | null>(null);
  equipoCargadoDesdeGarantia = signal(false);

  // Detalles del servicio (componentes del equipo)
  readonly detalles = signal<DetalleServicio[]>([]);
  readonly currentDetalle = signal<Partial<DetalleServicio>>({});
  readonly isAddingDetalle = signal(false);
  readonly isEditingDetalle = signal(false);
  readonly editingIndex = signal<number | null>(null);

  // Firma digital
  readonly hasSignature = signal(false);

  // Form
  servicioForm: FormGroup;

  // Tipos de ingreso - usando los valores del enum del backend
  readonly tiposIngreso = [
    { label: 'Cliente Trae Equipo', value: 'CLIENTE_TRAE' },
    { label: 'Empresa Busca Equipo', value: 'EMPRESA_BUSCA' }
  ];

  constructor() {
    this.servicioForm = this.fb.group({
      clienteId: [null, Validators.required],
      equipoId: [null, Validators.required],
      empleadoRecepcionId: [null, Validators.required],
      tipoIngreso: ['CLIENTE_TRAE', Validators.required],
      esGarantia: [false],
      servicioGarantiaId: [null],
      abonaVisita: [false],
      montoVisita: [0],
      montoPagado: [0],
      fallaReportada: [''],
      observaciones: [''],
      fechaRecepcion: [new Date(), Validators.required],
      fechaDevolucionPrevista: [null]
    });
  }

  ngOnInit(): void {
    this.loadClientes();
    this.loadEquipos();
    this.loadEmpleados();

    // Cuando cambia el tipo de ingreso a EMPRESA_BUSCA, activar abonaVisita
    this.servicioForm.get('tipoIngreso')?.valueChanges.subscribe(tipo => {
      if (tipo === 'EMPRESA_BUSCA') {
        this.servicioForm.patchValue({ abonaVisita: true });
      }
    });

    // Cuando cambia esGarantia, manejar validaciones y carga de servicios
    this.servicioForm.get('esGarantia')?.valueChanges.subscribe(esGarantia => {
      this.onGarantiaChange(esGarantia);
    });
  }

  ngAfterViewInit(): void {
    // Pequeño delay para asegurar que el canvas esté renderizado
    setTimeout(() => {
      this.setupCanvas();
    }, 100);
  }

  /**
   * Método para buscar clientes dinámicamente mientras el usuario escribe
   * Se activa con el evento (completeMethod) del p-autocomplete
   */
  buscarClientes(event: any): void {
    const query = event.query || '';

    console.log('🔍 Buscando clientes con término:', query);

    // Si el término tiene menos de 2 caracteres, no buscar
    if (query.length < 2) {
      this.clientes.set([]);
      return;
    }

    // Llamar al servicio de autocompletado
    this.clientService.autocompleteClients({ termino: query, limite: 10 }).subscribe({
      next: (clientes) => {
        console.log('✅ Clientes encontrados:', clientes.length);
        this.clientes.set(clientes);

        if (clientes.length === 0) {
          this.messageService.add({
            severity: 'info',
            summary: 'Sin resultados',
            detail: 'No se encontraron clientes con ese criterio',
            life: 2000
          });
        }
      },
      error: (error) => {
        console.error('❌ Error al buscar clientes:', error);
        this.clientes.set([]);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al buscar clientes'
        });
      }
    });
  }

  /**
   * Se ejecuta cuando el usuario selecciona un cliente del autocomplete
   */
  onClienteSelected(event: any): void {
    const cliente = event.value as ClientListDto;
    console.log('✅ Cliente seleccionado:', cliente);

    // Actualizar el formControl con el ID del cliente seleccionado
    this.servicioForm.patchValue({ clienteId: cliente.id });
    this.selectedCliente.set(cliente);

    // Cargar equipos del cliente seleccionado
    this.loadEquiposByCliente(cliente.id);

    // Si está marcado como garantía, cargar servicios previos
    if (this.servicioForm.get('esGarantia')?.value) {
      this.loadServiciosPreviosCliente(cliente.id);
    }
  }

  /**
   * Se ejecuta cuando el usuario limpia la selección
   */
  onClienteCleared(): void {
    console.log('🗑️ Cliente limpiado');
    this.servicioForm.patchValue({ clienteId: null, equipoId: null, servicioGarantiaId: null });
    this.selectedCliente.set(null);
    this.equipos.set([]); // Limpiar equipos
    this.serviciosPrevios.set([]); // Limpiar servicios previos
    this.selectedServicioPrevio.set(null);
    this.equipoCargadoDesdeGarantia.set(false);
  }

  /**
   * Maneja el cambio del checkbox esGarantia
   */
  onGarantiaChange(esGarantia: boolean): void {
    const servicioGarantiaIdControl = this.servicioForm.get('servicioGarantiaId');

    if (esGarantia) {
      // Hacer que servicioGarantiaId sea requerido cuando esGarantia es true
      servicioGarantiaIdControl?.setValidators([Validators.required]);

      // Cargar servicios del cliente si ya hay uno seleccionado
      const clienteId = this.servicioForm.get('clienteId')?.value;
      if (clienteId) {
        this.loadServiciosPreviosCliente(clienteId);
      }
    } else {
      // Quitar validación required cuando esGarantia es false
      servicioGarantiaIdControl?.clearValidators();
      servicioGarantiaIdControl?.patchValue(null);
      this.serviciosPrevios.set([]);
      this.selectedServicioPrevio.set(null);

      // Si el equipo fue cargado desde garantía, permitir selección manual
      if (this.equipoCargadoDesdeGarantia()) {
        this.servicioForm.get('equipoId')?.enable();
        this.servicioForm.patchValue({ equipoId: null });
        this.equipoCargadoDesdeGarantia.set(false);
      }
    }

    servicioGarantiaIdControl?.updateValueAndValidity();
  }

  /**
   * Carga los servicios previos del cliente (elegibles para garantía)
   */
  loadServiciosPreviosCliente(clienteId: number): void {
    console.log('🔍 Cargando servicios previos del cliente:', clienteId);

    this.servicioService.obtenerServiciosElegiblesParaGarantia(clienteId).subscribe({
      next: (servicios) => {
        console.log('✅ Servicios elegibles para garantía:', servicios);
        this.serviciosPrevios.set(servicios);

        if (servicios.length === 0) {
          this.messageService.add({
            severity: 'info',
            summary: 'Sin servicios',
            detail: 'Este cliente no tiene servicios TERMINADOS elegibles para garantía (últimos 90 días)',
            life: 4000
          });
        }
      },
      error: (error) => {
        console.error('❌ Error al cargar servicios previos:', error);
        this.serviciosPrevios.set([]);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los servicios del cliente'
        });
      }
    });
  }

  /**
   * Se ejecuta cuando el usuario selecciona un servicio previo
   */
  onServicioPrevioSelected(event: any): void {
    const servicio = event.value as ServicioList;
    console.log('✅ Servicio previo seleccionado:', servicio);

    // Actualizar el formControl con el ID del servicio de garantía
    this.servicioForm.patchValue({ servicioGarantiaId: servicio.id });
    this.selectedServicioPrevio.set(servicio);

    // Cargar automáticamente el equipo del servicio anterior
    if (servicio.equipoId) {
      this.servicioForm.patchValue({ equipoId: servicio.equipoId });
      this.equipoCargadoDesdeGarantia.set(true);

      // Deshabilitar el select de equipos para evitar cambios
      this.servicioForm.get('equipoId')?.disable();

      this.messageService.add({
        severity: 'success',
        summary: 'Equipo cargado',
        detail: `Se ha cargado automáticamente el equipo: ${servicio.equipoDescripcion}`,
        life: 4000
      });
    }
  }

  /**
   * Se ejecuta cuando el usuario limpia la selección de servicio previo
   */
  onServicioPrevioCleared(): void {
    console.log('🗑️ Servicio previo limpiado');
    this.servicioForm.patchValue({ servicioGarantiaId: null });
    this.selectedServicioPrevio.set(null);

    // Permitir selección manual de equipo nuevamente
    if (this.equipoCargadoDesdeGarantia()) {
      this.servicioForm.get('equipoId')?.enable();
      this.servicioForm.patchValue({ equipoId: null });
      this.equipoCargadoDesdeGarantia.set(false);
    }
  }

  loadClientes(): void {
    // Ya no necesitamos cargar clientes al inicio
    // El autocomplete los buscará dinámicamente
    console.log('📋 Componente de clientes con autocomplete dinámico listo');
  }

  loadEquipos(): void {
    // Ya no cargamos equipos al inicio
    // Se cargarán cuando el usuario seleccione un cliente
    console.log('📦 Equipos se cargarán cuando se seleccione un cliente');
  }

  /**
   * Carga los equipos de un cliente específico
   */
  loadEquiposByCliente(clienteId: number): void {
    console.log('🔍 Cargando equipos del cliente:', clienteId);

    this.equipoService.getEquiposByCliente(clienteId).subscribe({
      next: (equipos) => {
        console.log('✅ Equipos cargados:', equipos);
        this.equipos.set(equipos);

        if (equipos.length === 0) {
          this.messageService.add({
            severity: 'info',
            summary: 'Sin equipos',
            detail: 'Este cliente no tiene equipos registrados. Puede crear uno en la gestión de equipos.',
            life: 4000
          });
        }
      },
      error: (error) => {
        console.error('❌ Error al cargar equipos:', error);
        this.equipos.set([]);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los equipos del cliente'
        });
      }
    });
  }

  loadEmpleados(): void {
    console.log('Cargando empleados...');
    this.employeeService.getActiveEmployees().subscribe({
      next: (empleados) => {
        console.log('Empleados cargados:', empleados);
        this.empleados.set(empleados);
      },
      error: (error) => {
        console.error('Error al cargar empleados:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los empleados'
        });
      }
    });
  }

  // Manejo de detalles (componentes del equipo)
  toggleAddingDetalle(): void {
    this.isAddingDetalle.set(!this.isAddingDetalle());
    if (!this.isAddingDetalle()) {
      this.currentDetalle.set({});
    } else {
      // Cuando se abre el form de agregar, resetear el estado de edición
      this.isEditingDetalle.set(false);
      this.editingIndex.set(null);
      this.currentDetalle.set({ presente: false }); // Inicializar presente como false por defecto
    }
  }

  updateCurrentDetalle(field: string, value: any): void {
    this.currentDetalle.update(current => ({
      ...current,
      [field]: value
    }));
  }

  cancelDetalle(): void {
    this.isAddingDetalle.set(false);
    this.isEditingDetalle.set(false);
    this.editingIndex.set(null);
    this.currentDetalle.set({});
  }

  addDetalle(): void {
    const detalle = this.currentDetalle();

    if (!detalle.componente) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Por favor, completa el nombre del componente'
      });
      return;
    }

    const newDetalle: DetalleServicio = {
      componente: detalle.componente,
      presente: detalle.presente ?? false, // Por defecto false
      comentario: detalle.comentario
    };

    this.detalles.update(detalles => [...detalles, newDetalle]);
    this.currentDetalle.set({});
    this.isAddingDetalle.set(false);

    this.messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Componente agregado correctamente'
    });
  }

  editDetalle(index: number): void {
    const detalle = this.detalles()[index];
    this.currentDetalle.set({ ...detalle });
    this.editingIndex.set(index);
    this.isEditingDetalle.set(true);
    this.isAddingDetalle.set(false);
  }

  updateDetalle(): void {
    const detalle = this.currentDetalle();
    const index = this.editingIndex();

    if (index === null) {
      return;
    }

    if (!detalle.componente) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Advertencia',
        detail: 'Por favor, completa el nombre del componente'
      });
      return;
    }

    const updatedDetalle: DetalleServicio = {
      componente: detalle.componente,
      presente: detalle.presente ?? false,
      comentario: detalle.comentario
    };

    this.detalles.update(detalles => {
      const newDetalles = [...detalles];
      newDetalles[index] = updatedDetalle;
      return newDetalles;
    });

    this.cancelDetalle();

    this.messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Componente actualizado correctamente'
    });
  }

  removeDetalle(index: number): void {
    this.detalles.update(detalles => detalles.filter((_, i) => i !== index));
    this.messageService.add({
      severity: 'info',
      summary: 'Información',
      detail: 'Componente eliminado'
    });
  }

  // Configuración del canvas para firma
  setupCanvas(): void {
    if (!this.signatureCanvas || !this.signatureCanvas.nativeElement) {
      console.warn('Canvas no disponible para inicializar');
      return;
    }

    const canvas = this.signatureCanvas.nativeElement;

    // Limpiar instancia previa si existe
    if (this.signaturePad) {
      this.signaturePad.off();
    }

    // Configurar dimensiones del canvas para que coincidan con su tamaño CSS
    const rect = canvas.getBoundingClientRect();
    const ratio = Math.max(window.devicePixelRatio || 1, 1);

    canvas.width = rect.width * ratio;
    canvas.height = rect.height * ratio;

    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.scale(ratio, ratio);
      // Establecer estilo CSS explícitamente
      canvas.style.width = `${rect.width}px`;
      canvas.style.height = `${rect.height}px`;
    }

    // Inicializar SignaturePad
    this.signaturePad = new SignaturePad(canvas, {
      backgroundColor: 'rgb(255, 255, 255)',
      penColor: 'rgb(0, 0, 0)',
      minWidth: 1,
      maxWidth: 3,
      throttle: 8,
      minDistance: 2,
      velocityFilterWeight: 0.5
    });

    // Detectar cuando el usuario empieza a firmar
    this.signaturePad.addEventListener('beginStroke', () => {
      this.hasSignature.set(true);
    });

    console.log('Canvas de firma inicializado correctamente', {
      width: canvas.width,
      height: canvas.height,
      ratio: ratio
    });
  }

  clearSignature(): void {
    if (this.signaturePad) {
      this.signaturePad.clear();
      this.hasSignature.set(false);
    }
  }

  getSignatureData(): string | null {
    if (this.signaturePad && !this.signaturePad.isEmpty()) {
      return this.signaturePad.toDataURL('image/png');
    }
    return null;
  }

  // Helpers
  getClienteDisplay(cliente: ClientListDto): string {
    return `${cliente.nombreCompleto} (${cliente.documento})`;
  }

  getEquipoDisplay(equipo: EquipoListDto): string {
    if (!equipo) return '';

    let display = `${equipo.tipoEquipo} ${equipo.marca}`;
    if (equipo.modelo) {
      display += ` ${equipo.modelo}`;
    }
    if (equipo.numeroSerie) {
      display += ` (S/N: ${equipo.numeroSerie})`;
    }
    return display;
  }

  getEmpleadoDisplay(empleado: EmployeeListDto): string {
    return `${empleado.nombreCompleto}`;
  }

  getServicioPrevioDisplay(servicio: ServicioList): string {
    return `${servicio.numeroServicio} - ${servicio.equipoDescripcion} (${new Date(servicio.fechaRecepcion).toLocaleDateString()})`;
  }

  // Helpers para el resumen
  getSelectedCliente(): string {
    // Usar el cliente seleccionado del signal en lugar de buscarlo en el array
    const cliente = this.selectedCliente();
    return cliente?.nombreCompleto || '-';
  }

  getSelectedEmpleado(): string {
    const empleado = this.empleados().find(e => e.id === this.servicioForm.value.empleadoRecepcionId);
    return empleado ? this.getEmpleadoDisplay(empleado) : '-';
  }

  getSelectedEquipo(): string {
    const equipo = this.equipos().find(e => e.id === this.servicioForm.value.equipoId);
    return equipo ? this.getEquipoDisplay(equipo) : '-';
  }

  // Step tracking
  onStepChange(stepValue: number): void {
    // El paso viene de p-stepper (1-4), lo convertimos a índice (0-3)
    this.currentStep.set(stepValue - 1);
    this.activeStep = stepValue;

    // Si llegamos al paso 4 (confirmación), inicializar el canvas de firma
    if (stepValue === 4) {
      setTimeout(() => {
        this.setupCanvas();
      }, 100);
    }
  }

  // Navegación entre pasos
  nextStep(): void {
    if (this.activeStep < 4) {
      this.activeStep = this.activeStep + 1;
      this.currentStep.set(this.activeStep - 1);

      // Si llegamos al paso 4 (confirmación), inicializar el canvas de firma
      if (this.activeStep === 4) {
        setTimeout(() => {
          this.setupCanvas();
        }, 100);
      }
    }
  }

  previousStep(): void {
    if (this.activeStep > 1) {
      this.activeStep = this.activeStep - 1;
      this.currentStep.set(this.activeStep - 1);
    }
  }

  // Cancelar
  onCancel(): void {
    this.router.navigate(['/servicios']);
  }

  // Guardar servicio
  saveServicio(): void {
    if (!this.servicioForm.valid) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Por favor, completa todos los campos requeridos'
      });
      return;
    }

    this.isSaving.set(true);
    const formValue = this.servicioForm.value;

    // Formatear fechas - validar que sean fechas válidas antes de convertir
    let fechaRecepcion: string;
    if (formValue.fechaRecepcion instanceof Date) {
      fechaRecepcion = formValue.fechaRecepcion.toISOString().split('T')[0];
    } else if (typeof formValue.fechaRecepcion === 'string') {
      fechaRecepcion = formValue.fechaRecepcion;
    } else {
      fechaRecepcion = new Date().toISOString().split('T')[0];
    }

    let fechaDevolucionPrevista: string | undefined = undefined;
    if (formValue.fechaDevolucionPrevista) {
      if (formValue.fechaDevolucionPrevista instanceof Date) {
        const fecha = formValue.fechaDevolucionPrevista;
        if (!isNaN(fecha.getTime())) {
          fechaDevolucionPrevista = fecha.toISOString().split('T')[0];
        }
      } else if (typeof formValue.fechaDevolucionPrevista === 'string') {
        fechaDevolucionPrevista = formValue.fechaDevolucionPrevista;
      }
    }

    // Extraer solo la parte base64 de la firma (sin el prefijo data:image/png;base64,)
    const signatureData = this.getSignatureData();
    const firmaBase64 = signatureData
      ? signatureData.split(',')[1]
      : undefined;

    // Obtener equipoId del form (puede estar deshabilitado si se cargó desde garantía)
    const equipoId = this.equipoCargadoDesdeGarantia()
      ? this.servicioForm.getRawValue().equipoId
      : formValue.equipoId;

    const createData: ServicioCreateDto = {
      clienteId: formValue.clienteId,
      equipoId: equipoId,
      empleadoRecepcionId: formValue.empleadoRecepcionId,
      tipoIngreso: formValue.tipoIngreso,
      esGarantia: formValue.esGarantia,
      servicioGarantiaId: formValue.servicioGarantiaId,
      abonaVisita: formValue.abonaVisita,
      montoVisita: formValue.montoVisita || 0,
      montoPagado: formValue.montoPagado || 0,
      fallaReportada: formValue.fallaReportada,
      observaciones: formValue.observaciones,
      fechaRecepcion: fechaRecepcion,
      fechaDevolucionPrevista: fechaDevolucionPrevista,
      detalles: this.detalles(),
      firmaIngreso: firmaBase64
    };

    this.servicioService.crearServicio(createData).subscribe({
      next: (response) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Servicio creado exitosamente',
          detail: `Número de servicio: ${response.numeroServicio}`,
          life: 5000
        });
        setTimeout(() => {
          this.router.navigate(['/servicios']);
        }, 1500);
        this.isSaving.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.message || 'Error al crear servicio'
        });
        this.isSaving.set(false);
      }
    });
  }
}
