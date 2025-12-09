export enum EstadoServicio {
  RECIBIDO = 'RECIBIDO',
  ESPERANDO_EVALUACION_GARANTIA = 'ESPERANDO_EVALUACION_GARANTIA',
  PRESUPUESTADO = 'PRESUPUESTADO',
  APROBADO = 'APROBADO',
  EN_REPARACION = 'EN_REPARACION',
  TERMINADO = 'TERMINADO',
  RECHAZADO = 'RECHAZADO',
  GARANTIA_SIN_REPARACION = 'GARANTIA_SIN_REPARACION',
  GARANTIA_RECHAZADA = 'GARANTIA_RECHAZADA'
}

export interface DetalleServicio {
  id?: number;
  componente: string;
  presente: boolean;
  comentario?: string;
}

export interface ServicioList {
  id: number;
  numeroServicio: string;
  clienteId?: number;
  clienteNombre: string;
  clienteDocumento: string;
  equipoId?: number;
  equipoDescripcion: string;
  equipoNumeroSerie: string;
  equipoMarca?: string;
  equipoModelo?: string;
  empleadoRecepcionNombre: string;
  tipoIngreso: string;
  esGarantia: boolean;
  servicioGarantiaId?: number;
  servicioGarantiaNumero?: string;
  tecnicoEvaluacionId?: number;
  tecnicoEvaluacionNombre?: string;
  tecnicoAsignadoId?: number;
  tecnicoAsignadoNombre?: string;
  abonaVisita: boolean;
  montoVisita: number;
  montoPagado: number;
  estado: EstadoServicio;
  fechaCreacion: string;
  fechaRecepcion: string;
  fechaDevolucionPrevista?: string;
  fechaDevolucionReal?: string;
}

export interface ServicioResponse {
  id: number;
  numeroServicio: string;
  clienteId: number;
  clienteNombre: string;
  clienteDocumento: string;
  equipoId: number;
  equipoDescripcion: string;
  equipoNumeroSerie: string;
  equipoMarca?: string;
  equipoModelo?: string;
  empleadoRecepcionNombre: string;
  tipoIngreso: string;
  esGarantia: boolean;
  servicioGarantiaId?: number;
  servicioGarantiaNumero?: string;
  garantiaDentroPlazo?: boolean;
  garantiaCumpleCondiciones?: boolean;
  observacionesGarantia?: string;
  tecnicoEvaluacionId?: number;
  tecnicoEvaluacionNombre?: string;
  fechaEvaluacionGarantia?: string;
  observacionesEvaluacionGarantia?: string;
  abonaVisita: boolean;
  montoVisita: number;
  montoPagado: number;
  estado: EstadoServicio;
  fallaReportada?: string;
  observaciones?: string;
  fechaCreacion: string;
  fechaRecepcion: string;
  fechaDevolucionPrevista?: string;
  fechaDevolucionReal?: string;
  detalles: DetalleServicio[];
  firmaIngreso?: string; // Base64 de la firma del cliente
}

export interface ServicioCreateDto {
  clienteId: number;
  equipoId: number;
  empleadoRecepcionId: number;
  tipoIngreso: string;
  esGarantia: boolean;
  servicioGarantiaId?: number;
  garantiaDentroPlazo?: boolean;
  garantiaCumpleCondiciones?: boolean;
  observacionesGarantia?: string;
  tecnicoEvaluacionId?: number;
  observacionesEvaluacionGarantia?: string;
  abonaVisita: boolean;
  montoVisita: number;
  montoPagado: number;
  fallaReportada?: string;
  observaciones?: string;
  fechaRecepcion: string;
  fechaDevolucionPrevista?: string;
  detalles: DetalleServicio[];
  firmaIngreso?: string; // Base64 de la firma del cliente
}

export interface ServicioEvent {
  tipo: 'CREADO' | 'ACTUALIZADO' | 'ESTADO_CAMBIADO' | 'ELIMINADO';
  servicioId: number;
  numeroServicio: string;
  estadoAnterior?: EstadoServicio;
  estadoNuevo?: EstadoServicio;
  timestamp: string;
  servicio?: ServicioList;
}

export interface PaginatedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}
