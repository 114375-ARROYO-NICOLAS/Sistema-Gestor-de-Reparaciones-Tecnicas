package com.sigret.dtos.ordenTrabajo;

import com.sigret.enums.EstadoOrdenTrabajo;
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
public class OrdenTrabajoListDto {

    private Long id;
    private String numeroServicio;
    private String clienteNombre;
    private String equipoDescripcion;
    private String empleadoNombre;
    private EstadoOrdenTrabajo estado;
    private LocalDateTime fechaCreacion;
    private LocalDate fechaComienzo;
    private LocalDate fechaFin;
    private BigDecimal montoTotalFinal;
    private Boolean esSinCosto;
}
