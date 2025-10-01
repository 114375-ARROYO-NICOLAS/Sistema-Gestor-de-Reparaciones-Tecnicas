package com.sigret.dtos.ordenTrabajo;

import com.sigret.enums.EstadoOrdenTrabajo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajoCreateDto {

    @NotNull(message = "El servicio es obligatorio")
    private Long servicioId;

    private Long presupuestoId;

    @NotNull(message = "El empleado es obligatorio")
    private Long empleadoId;

    private BigDecimal montoTotalRepuestos = BigDecimal.ZERO;

    private BigDecimal montoExtras = BigDecimal.ZERO;

    private String observacionesExtras;

    private Boolean esSinCosto = false;

    private EstadoOrdenTrabajo estado = EstadoOrdenTrabajo.PENDIENTE;
}
