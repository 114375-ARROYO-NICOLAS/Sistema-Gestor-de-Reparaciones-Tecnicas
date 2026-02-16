export type TipoReferencia = 'SERVICIO' | 'PRESUPUESTO' | 'ORDEN_TRABAJO';

export interface Notificacion {
  id: number;
  mensaje: string;
  tipo: string;
  leida: boolean;
  fechaCreacion: string;
  referenciaId?: number;
  tipoReferencia?: TipoReferencia;
  icono?: string;
  severidad?: string;
}
