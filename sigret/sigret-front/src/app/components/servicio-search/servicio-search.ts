import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ServicioService } from '../../services/servicio.service';
import { ClientService } from '../../services/client.service';
import { EmployeeService } from '../../services/employee.service';
import { TipoEquipoService } from '../../services/tipo-equipo.service';
import { ServicioList, EstadoServicio } from '../../models/servicio.model';
import { MessageService } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { Tag } from 'primeng/tag';
import { InputText } from 'primeng/inputtext';
import { AutoComplete } from 'primeng/autocomplete';
import { Divider } from 'primeng/divider';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Checkbox } from 'primeng/checkbox';
import { DatePicker } from 'primeng/datepicker';

interface FilterOption {
  label: string;
  value: string;
}

@Component({
  selector: 'app-servicio-search',
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    Button,
    Card,
    Tag,
    InputText,
    AutoComplete,
    Divider,
    ProgressSpinner,
    Checkbox,
    DatePicker
  ],
  templateUrl: './servicio-search.html',
  styleUrl: './servicio-search.scss'
})
export class ServicioSearch implements OnInit {
  private readonly router = inject(Router);
  private readonly servicioService = inject(ServicioService);
  private readonly clientService = inject(ClientService);
  private readonly employeeService = inject(EmployeeService);
  private readonly tipoEquipoService = inject(TipoEquipoService);
  private readonly messageService = inject(MessageService);

  private readonly MAX_VISIBLE = 4;

  // Signals
  readonly showMobileFilters = signal(false);
  readonly loading = signal<boolean>(true);
  readonly servicios = signal<ServicioList[]>([]);

  // Opciones para filtros (cargadas del backend)
  readonly clientesSugerencias = signal<string[]>([]);
  readonly estadosOptions = signal<FilterOption[]>([]);
  readonly empleadosOptions = signal<FilterOption[]>([]);
  readonly tiposEquipoOptions = signal<FilterOption[]>([]);

  readonly garantiaOptions = signal<{ label: string; value: boolean | null }[]>([
    { label: 'Todos', value: null },
    { label: 'Solo Garantías', value: true },
    { label: 'Sin Garantías', value: false }
  ]);

  // "Ver más" toggles
  readonly showAllEstados = signal(false);
  readonly showAllEmpleados = signal(false);
  readonly showAllTiposEquipo = signal(false);

  // Opciones visibles (primeras 4 o todas)
  readonly estadosVisibles = computed(() =>
    this.showAllEstados() ? this.estadosOptions() : this.estadosOptions().slice(0, this.MAX_VISIBLE)
  );
  readonly empleadosVisibles = computed(() =>
    this.showAllEmpleados() ? this.empleadosOptions() : this.empleadosOptions().slice(0, this.MAX_VISIBLE)
  );
  readonly tiposEquipoVisibles = computed(() =>
    this.showAllTiposEquipo() ? this.tiposEquipoOptions() : this.tiposEquipoOptions().slice(0, this.MAX_VISIBLE)
  );

  // Filtros actuales como signals
  readonly busqueda = signal<string>('');
  readonly estadosFiltro = signal<string[]>([]);
  readonly clienteFiltro = signal<string>('');
  readonly empleadosFiltro = signal<string[]>([]);
  readonly tiposEquipoFiltro = signal<string[]>([]);
  readonly garantiaFiltro = signal<boolean | null>(null);
  readonly fechaDesde = signal<Date | null>(null);
  readonly fechaHasta = signal<Date | null>(null);

  // Computed para servicios filtrados
  readonly serviciosFiltrados = computed(() => {
    let servicios = this.servicios();

    // Filtro por búsqueda (solo número de servicio)
    const busquedaValue = this.busqueda();
    if (busquedaValue.trim()) {
      const busquedaLower = busquedaValue.toLowerCase();
      servicios = servicios.filter(s =>
        s.numeroServicio.toLowerCase().includes(busquedaLower)
      );
    }

    // Filtro por estados
    const estados = this.estadosFiltro();
    if (estados.length > 0) {
      servicios = servicios.filter(s => estados.includes(s.estado));
    }

    // Filtro por cliente (por nombre)
    const cliente = this.clienteFiltro();
    if (cliente.trim()) {
      const clienteLower = cliente.toLowerCase();
      servicios = servicios.filter(s =>
        s.clienteNombre.toLowerCase().includes(clienteLower)
      );
    }

    // Filtro por empleados (por nombre)
    const empleados = this.empleadosFiltro();
    if (empleados.length > 0) {
      servicios = servicios.filter(s => empleados.includes(s.empleadoRecepcionNombre));
    }

    // Filtro por tipos de equipo (busca en la descripción del equipo)
    const tiposEquipo = this.tiposEquipoFiltro();
    if (tiposEquipo.length > 0) {
      servicios = servicios.filter(s =>
        tiposEquipo.some(tipo =>
          s.equipoDescripcion.toLowerCase().includes(tipo.toLowerCase())
        )
      );
    }

    // Filtro por garantía
    const garantia = this.garantiaFiltro();
    if (garantia !== null) {
      servicios = servicios.filter(s => s.esGarantia === garantia);
    }

    // Filtro por rango de fecha de creación
    const desde = this.fechaDesde();
    const hasta = this.fechaHasta();
    if (desde) {
      const desdeTime = new Date(desde.getFullYear(), desde.getMonth(), desde.getDate()).getTime();
      servicios = servicios.filter(s => {
        const fechaCreacion = new Date(s.fechaCreacion).getTime();
        return fechaCreacion >= desdeTime;
      });
    }
    if (hasta) {
      const hastaTime = new Date(hasta.getFullYear(), hasta.getMonth(), hasta.getDate(), 23, 59, 59, 999).getTime();
      servicios = servicios.filter(s => {
        const fechaCreacion = new Date(s.fechaCreacion).getTime();
        return fechaCreacion <= hastaTime;
      });
    }

    return servicios;
  });

  ngOnInit(): void {
    this.cargarServicios();
    this.cargarOpcionesFiltros();
  }

  private cargarServicios(): void {
    this.loading.set(true);

    this.servicioService.obtenerTodosLosServicios().subscribe({
      next: (servicios) => {
        this.servicios.set(servicios);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error al cargar servicios:', err);
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudieron cargar los servicios'
        });
      }
    });
  }

  private cargarOpcionesFiltros(): void {
    // Cargar estados desde el backend
    this.servicioService.obtenerEstados().subscribe({
      next: (estados) => {
        this.estadosOptions.set(estados);
      },
      error: (err) => console.error('Error al cargar estados:', err)
    });

    // Cargar empleados
    this.employeeService.getEmployees({ size: 1000 }).subscribe({
      next: (response) => {
        this.empleadosOptions.set(response.content.map(e => ({
          label: e.nombreCompleto,
          value: e.nombreCompleto
        })));
      },
      error: (err) => console.error('Error al cargar empleados:', err)
    });

    // Cargar tipos de equipo
    this.tipoEquipoService.getAllTiposEquipo().subscribe({
      next: (tipos) => {
        this.tiposEquipoOptions.set(tipos.map(t => ({
          label: t.descripcion,
          value: t.descripcion
        })));
      },
      error: (err) => console.error('Error al cargar tipos de equipo:', err)
    });
  }

  // Toggle helpers para checkboxes
  toggleEstado(value: string): void {
    this.estadosFiltro.update(current =>
      current.includes(value) ? current.filter(v => v !== value) : [...current, value]
    );
  }

  toggleEmpleado(value: string): void {
    this.empleadosFiltro.update(current =>
      current.includes(value) ? current.filter(v => v !== value) : [...current, value]
    );
  }

  toggleTipoEquipo(value: string): void {
    this.tiposEquipoFiltro.update(current =>
      current.includes(value) ? current.filter(v => v !== value) : [...current, value]
    );
  }

  isEstadoSelected(value: string): boolean {
    return this.estadosFiltro().includes(value);
  }

  isEmpleadoSelected(value: string): boolean {
    return this.empleadosFiltro().includes(value);
  }

  isTipoEquipoSelected(value: string): boolean {
    return this.tiposEquipoFiltro().includes(value);
  }

  aplicarFiltros(): void {
    const totalFiltrados = this.serviciosFiltrados().length;
    const totalServicios = this.servicios().length;

    if (totalFiltrados === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Sin resultados',
        detail: 'No se encontraron servicios con los filtros aplicados'
      });
    } else if (totalFiltrados === totalServicios) {
      this.messageService.add({
        severity: 'info',
        summary: 'Filtros aplicados',
        detail: `Mostrando todos los servicios (${totalServicios})`
      });
    } else {
      this.messageService.add({
        severity: 'success',
        summary: 'Filtros aplicados',
        detail: `${totalFiltrados} de ${totalServicios} servicios encontrados`
      });
    }
  }

  buscarClientes(event: { query: string }): void {
    const query = event.query;
    if (query && query.length >= 2) {
      this.clientService.getClients({ filtro: query, size: 20 }).subscribe({
        next: (response) => {
          this.clientesSugerencias.set(response.content.map(c => c.nombreCompleto));
        },
        error: (err) => console.error('Error al buscar clientes:', err)
      });
    } else {
      this.clientesSugerencias.set([]);
    }
  }

  limpiarFiltros(): void {
    this.busqueda.set('');
    this.estadosFiltro.set([]);
    this.clienteFiltro.set('');
    this.empleadosFiltro.set([]);
    this.tiposEquipoFiltro.set([]);
    this.garantiaFiltro.set(null);
    this.fechaDesde.set(null);
    this.fechaHasta.set(null);

    this.messageService.add({
      severity: 'info',
      summary: 'Filtros limpiados',
      detail: 'Se han eliminado todos los filtros'
    });
  }

  getEstadoSeverity(estado: EstadoServicio): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
    const severityMap: Record<EstadoServicio, 'success' | 'info' | 'warn' | 'danger' | 'secondary'> = {
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

  verDetalle(servicioId: number): void {
    this.router.navigate(['/servicios', servicioId]);
  }
}
