import { EstadoServicio } from './servicio.model';
import { ItemEvaluacionGarantia } from './item-evaluacion-garantia.model';

export interface ServicioUpdateDto {
  // Estado
  estado?: EstadoServicio;

  // Fechas
  fechaDevolucionPrevista?: string; // LocalDate en backend
  fechaDevolucionReal?: string; // LocalDate en backend

  // Pago
  abonaVisita?: boolean;
  montoVisita?: number; // BigDecimal en backend
  montoPagado?: number; // BigDecimal en backend

  // Tipo de ingreso
  tipoIngreso?: string;

  // Firmas
  firmaIngreso?: string; // Base64
  firmaConformidad?: string; // Base64

  // Garantía
  esGarantia?: boolean;
  servicioGarantiaId?: number;
  garantiaDentroPlazo?: boolean;
  garantiaCumpleCondiciones?: boolean;
  observacionesGarantia?: string;
  tecnicoEvaluacionId?: number;
  observacionesEvaluacionGarantia?: string;

  // Items de evaluación de garantía (solo cuando cumple condiciones)
  itemsEvaluacionGarantia?: ItemEvaluacionGarantia[];
}
