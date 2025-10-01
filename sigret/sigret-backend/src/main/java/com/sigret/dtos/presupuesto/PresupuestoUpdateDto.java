package com.sigret.dtos.presupuesto;

import com.sigret.enums.EstadoPresupuesto;
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
public class PresupuestoUpdateDto {

    private BigDecimal montoTotal;

    private LocalDate fechaVencimiento;

    private EstadoPresupuesto estado;

    private String observaciones;
}
