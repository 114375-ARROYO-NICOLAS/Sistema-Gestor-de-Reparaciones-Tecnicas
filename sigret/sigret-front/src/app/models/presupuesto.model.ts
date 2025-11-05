export interface Presupuesto {
  id: number;
  numeroPresupuesto: string;
  numeroServicio: string;
  clienteNombre: string;
  montoTotal: number;
  fechaVencimiento?: Date;
  estado: EstadoPresupuesto;
  fechaCreacion: Date;
  empleadoNombre?: string;
  equipoDescripcion?: string;
}

export enum EstadoPresupuesto {
  PENDIENTE = 'PENDIENTE',
  EN_CURSO = 'EN_CURSO',
  APROBADO = 'APROBADO',
  RECHAZADO = 'RECHAZADO'
}

export interface PresupuestoEvent {
  tipoEvento: 'CREADO' | 'ACTUALIZADO' | 'CAMBIO_ESTADO' | 'ELIMINADO';
  presupuestoId: number;
  servicioId: number;
  numeroServicio: string;
  estadoAnterior?: EstadoPresupuesto;
  estadoNuevo?: EstadoPresupuesto;
  presupuesto: Presupuesto;
}
