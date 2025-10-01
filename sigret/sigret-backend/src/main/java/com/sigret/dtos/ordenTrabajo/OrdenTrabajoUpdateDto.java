package com.sigret.dtos.ordenTrabajo;

import com.sigret.enums.EstadoOrdenTrabajo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajoUpdateDto {

    private Long presupuestoId;

    private Long empleadoId;

    private BigDecimal montoTotalRepuestos;

    private BigDecimal montoExtras;

    private String observacionesExtras;

    private Boolean esSinCosto;

    private EstadoOrdenTrabajo estado;
}
