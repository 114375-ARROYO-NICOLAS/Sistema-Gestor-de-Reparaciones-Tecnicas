export interface TendenciaMensual {
  mes: string;
  label: string;
  cantidad: number;
}

export interface DashboardEstadisticas {
  serviciosActivos: number;
  presupuestosPendientes: number;
  ordenesEnProgreso: number;
  tasaAprobacionPresupuestos: number;
  serviciosCompletadosMes: number;
  serviciosPorEstado: Record<string, number>;
  presupuestosPorEstado: Record<string, number>;
  ordenesTrabajoPorEstado: Record<string, number>;
  tendenciaMensual: TendenciaMensual[];
  ordenesPorEmpleado: Record<string, number>;
  garantiasPorTipoEquipo: Record<string, number>;
}
