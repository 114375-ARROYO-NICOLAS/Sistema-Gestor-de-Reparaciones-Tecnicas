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
import { Table, TableModule } from 'primeng/table';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { Tag } from 'primeng/tag';
import { InputText } from 'primeng/inputtext';
import { MultiSelect } from 'primeng/multiselect';
import { AutoComplete } from 'primeng/autocomplete';
import { Divider } from 'primeng/divider';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Accordion, AccordionPanel } from 'primeng/accordion';

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
    MultiSelect,
    AutoComplete,
    Divider,
    ProgressSpinner,
    Accordion,
    AccordionPanel
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

  // Signals
  readonly loading = signal<boolean>(true);
  readonly servicios = signal<ServicioList[]>([]);

  // Opciones para filtros
  readonly clientesSugerencias = signal<any[]>([]);
  readonly empleados = signal<any[]>([]);
  readonly tiposEquipo = signal<any[]>([]);
  readonly estadosOptions = signal<{ label: string; value: EstadoServicio }[]>([
    { label: 'Recibido', value: EstadoServicio.RECIBIDO },
    { label: 'Presupuestado', value: EstadoServicio.PRESUPUESTADO },
    { label: 'Aprobado', value: EstadoServicio.APROBADO },
    { label: 'En Reparación', value: EstadoServicio.EN_REPARACION },
    { label: 'Terminado', value: EstadoServicio.TERMINADO },
    { label: 'Rechazado', value: EstadoServicio.RECHAZADO }
  ]);
  readonly garantiaOptions = signal<{ label: string; value: boolean | null }[]>([
    { label: 'Todos', value: null },
    { label: 'Solo Garantías', value: true },
    { label: 'Sin Garantías', value: false }
  ]);

  // Filtros actuales como signals
  readonly busqueda = signal<string>('');
  readonly estadosFiltro = signal<EstadoServicio[]>([]);
  readonly clienteFiltro = signal<string>(''); // Cambiado a string para AutoComplete
  readonly empleadosFiltro = signal<string[]>([]); // Cambiado a string[] para filtrar por nombre
  readonly tiposEquipoFiltro = signal<string[]>([]); // Cambiado a string[] para filtrar por descripción
  readonly garantiaFiltro = signal<boolean | null>(null);

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
      servicios = servicios.filter(s => {
        // Buscar si algún tipo seleccionado está en la descripción del equipo
        return tiposEquipo.some(tipo =>
          s.equipoDescripcion.toLowerCase().includes(tipo.toLowerCase())
        );
      });
    }

    // Filtro por garantía
    const garantia = this.garantiaFiltro();
    if (garantia !== null) {
      servicios = servicios.filter(s => s.esGarantia === garantia);
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
    // Ya no cargamos clientes aquí, se cargan dinámicamente en buscarClientes()

    // Cargar empleados - usar nombreCompleto como value para filtrar
    this.employeeService.getEmployees({ size: 1000 }).subscribe({
      next: (response) => {
        this.empleados.set(response.content.map(e => ({
          label: e.nombreCompleto,
          value: e.nombreCompleto  // Usar nombre como valor para filtrar
        })));
      },
      error: (err) => console.error('Error al cargar empleados:', err)
    });

    // Cargar tipos de equipo - usar descripcion como value para filtrar
    this.tipoEquipoService.getAllTiposEquipo().subscribe({
      next: (tipos) => {
        this.tiposEquipo.set(tipos.map(t => ({
          label: t.descripcion,
          value: t.descripcion  // Usar descripción como valor para filtrar
        })));
      },
      error: (err) => console.error('Error al cargar tipos de equipo:', err)
    });
  }

  aplicarFiltros(): void {
    // Los filtros se aplican automáticamente mediante el computed signal
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

  buscarClientes(event: any): void {
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
      [EstadoServicio.GARANTIA_RECHAZADA]: 'danger'
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
