export interface OrdenTrabajo {
  id: number;
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
