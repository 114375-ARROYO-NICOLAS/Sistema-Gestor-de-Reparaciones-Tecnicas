package com.sigret.dtos.servicio;

import com.sigret.enums.EstadoServicio;
import com.sigret.enums.TipoIngreso;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioListDto {

    private Long id;
    private String numeroServicio;
    private String clienteNombre;
    private String clienteDocumento;
    private String equipoDescripcion;
    private String equipoNumeroSerie;
    private String empleadoRecepcionNombre;
    private TipoIngreso tipoIngreso;
    private String fallaReportada;
    private String observaciones;
    private Boolean esGarantia;
    private Boolean abonaVisita;
    private BigDecimal montoVisita;
    private BigDecimal montoPagado;
    private EstadoServicio estado;
    private LocalDateTime fechaCreacion;
    private LocalDate fechaRecepcion;
    private LocalDate fechaDevolucionPrevista;
    private LocalDate fechaDevolucionReal;

    // Campos de garantía
    private Long tecnicoEvaluacionId;
    private String tecnicoEvaluacionNombre;

    // Técnico asignado a la reparación (desde OrdenTrabajo)
    private Long tecnicoAsignadoId;
    private String tecnicoAsignadoNombre;
}
