export interface DetallePresupuesto {
  id?: number;
  item: string;
  cantidad: number;
  precioOriginal: number;
  precioAlternativo?: number;
}

export interface Presupuesto {
  id: number;
  numeroPresupuesto: string;
  servicioId: number;
  numeroServicio: string;
  clienteNombre: string;
  equipoDescripcion?: string;
  empleadoId?: number;
  empleadoNombre?: string;
  problema?: string;
  diagnostico?: string;
  detalles: DetallePresupuesto[];
  manoObra: number;
  montoRepuestosOriginal: number;
  montoRepuestosAlternativo?: number;
  montoTotalOriginal: number;
  montoTotalAlternativo?: number;
  mostrarOriginal: boolean;
  mostrarAlternativo: boolean;
  tipoConfirmado?: string;
  fechaConfirmacion?: Date;
  canalConfirmacion?: string;
  fechaVencimiento?: Date;
  fechaSolicitud?: Date;
  fechaPactada?: Date;
  estado: EstadoPresupuesto;
  observaciones?: string;
  fechaCreacion: Date;
}

export interface PresupuestoCreateDto {
  servicioId: number;
  empleadoId: number;
  diagnostico?: string;
  detalles: DetallePresupuesto[];
  manoObra: number;
  montoRepuestosOriginal?: number;
  montoRepuestosAlternativo?: number;
  montoTotalOriginal?: number;
  montoTotalAlternativo?: number;
  mostrarOriginal?: boolean;
  mostrarAlternativo?: boolean;
  fechaVencimiento?: string;
  fechaSolicitud?: string;
  fechaPactada?: string;
  estado?: EstadoPresupuesto;
}

export enum EstadoPresupuesto {
  PENDIENTE = 'PENDIENTE',
  EN_CURSO = 'EN_CURSO',
  LISTO = 'LISTO',
  ENVIADO = 'ENVIADO',
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

export interface EnvioPresupuestoDto {
  presupuestoId: number;
  mostrarOriginal: boolean;
  mostrarAlternativo: boolean;
  mensajeAdicional?: string;
}

export interface PresupuestoPublico {
  numeroPresupuesto: string;
  nombreCliente: string;
  equipoDescripcion: string;
  fallaReportada?: string;
  diagnostico?: string;
  detalles: DetallePresupuesto[];
  montoTotalOriginal: number;
  montoTotalAlternativo?: number;
  manoObra: number;
  mostrarOriginal: boolean;
  mostrarAlternativo: boolean;
  estado: string;
  fechaCreacion: string;
}
