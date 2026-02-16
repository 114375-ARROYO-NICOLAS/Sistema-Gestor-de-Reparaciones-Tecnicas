/**
 * Item de la orden de trabajo original para mostrar en la evaluación
 */
export interface ItemServicioOriginal {
  repuestoId: number | null;
  item: string;
  cantidad: number;
  comentario: string | null;
}

/**
 * Item seleccionado en la evaluación de garantía (solo los que tienen falla)
 */
export interface ItemEvaluacionGarantia {
  repuestoId: number;
  comentario?: string;
}

/**
 * Item con estado de selección para el formulario de evaluación
 */
export interface ItemEvaluacionForm extends ItemServicioOriginal {
  seleccionado: boolean;
  comentarioEvaluacion?: string;
}
