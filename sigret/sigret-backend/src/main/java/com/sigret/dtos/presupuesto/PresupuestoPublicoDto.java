package com.sigret.dtos.presupuesto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoPublicoDto {

    private String numeroPresupuesto;
    private String nombreCliente;
    private String equipoDescripcion;
    private String fallaReportada;
    private String diagnostico;
    private List<DetallePresupuestoDto> detalles;
    private BigDecimal montoTotalOriginal;
    private BigDecimal montoTotalAlternativo;
    private BigDecimal manoObra;
    private Boolean mostrarOriginal;
    private Boolean mostrarAlternativo;
    private String estado;
    private LocalDate fechaCreacion;
    private LocalDate fechaVencimiento;
    private Boolean vencido;
}