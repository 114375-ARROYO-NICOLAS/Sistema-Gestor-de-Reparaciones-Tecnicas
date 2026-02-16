export interface OrdenTrabajo {
  id: number;
  numeroOrdenTrabajo: string;
  numeroServicio: string;
  clienteNombre: string;
  equipoDescripcion: string;
  empleadoNombre: string;
  estado: EstadoOrdenTrabajo;
  fechaCreacion: Date;
  fechaComienzo?: Date;
  fechaFin?: Date;
  montoTotalFinal?: number;
  esSinCosto: boolean;
}

export interface DetalleOrdenTrabajo {
  id: number;
  item: string;
  cantidad: number;
  comentario?: string;
  completado: boolean;
}

export interface OrdenTrabajoResponse {
  id: number;
  numeroOrdenTrabajo: string;
  servicioId: number;
  numeroServicio: string;
  clienteNombre: string;
  equipoDescripcion: string;
  presupuestoId?: number;
  numeroPresupuesto?: string;
  empleadoId: number;
  empleadoNombre: string;
  montoTotalRepuestos?: number;
  montoExtras?: number;
  observacionesExtras?: string;
  esSinCosto: boolean;
  estado: EstadoOrdenTrabajo;
  fechaCreacion: Date;
  fechaComienzo?: Date;
  fechaFin?: Date;
  montoTotalFinal?: number;
  diasReparacion?: number;
  detalles: DetalleOrdenTrabajo[];
}

export enum EstadoOrdenTrabajo {
  PENDIENTE = 'PENDIENTE',
  EN_PROGRESO = 'EN_PROGRESO',
  TERMINADA = 'TERMINADA',
  CANCELADA = 'CANCELADA'
}

export interface OrdenTrabajoEvent {
  tipoEvento: 'CREADO' | 'ACTUALIZADO' | 'CAMBIO_ESTADO' | 'ELIMINADO';
  ordenTrabajoId: number;
  servicioId: number;
  numeroServicio: string;
  estadoAnterior?: EstadoOrdenTrabajo;
  estadoNuevo?: EstadoOrdenTrabajo;
  ordenTrabajo: OrdenTrabajo;
}
