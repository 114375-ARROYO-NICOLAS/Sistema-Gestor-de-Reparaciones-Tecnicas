package com.sigret.dtos.presupuesto;

import com.sigret.enums.EstadoPresupuesto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoCreateDto {

    @NotNull(message = "El servicio es obligatorio")
    private Long servicioId;

    private String numeroPresupuesto;

    private BigDecimal montoTotal = BigDecimal.ZERO;

    private LocalDate fechaVencimiento;

    private EstadoPresupuesto estado = EstadoPresupuesto.PENDIENTE;

    private String observaciones;
}
