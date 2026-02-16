package com.sigret.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEstadisticasDto {

    // KPI Cards
    private long serviciosActivos;
    private long presupuestosPendientes;
    private long ordenesEnProgreso;
    private double tasaAprobacionPresupuestos;
    private long serviciosCompletadosMes;

    // Charts
    private Map<String, Long> serviciosPorEstado;
    private Map<String, Long> presupuestosPorEstado;
    private Map<String, Long> ordenesTrabajoPorEstado;
    private List<TendenciaMensualDto> tendenciaMensual;
    private Map<String, Long> ordenesPorEmpleado;
    private Map<String, Long> garantiasPorTipoEquipo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TendenciaMensualDto {
        private String mes;
        private String label;
        private long cantidad;
    }
}
