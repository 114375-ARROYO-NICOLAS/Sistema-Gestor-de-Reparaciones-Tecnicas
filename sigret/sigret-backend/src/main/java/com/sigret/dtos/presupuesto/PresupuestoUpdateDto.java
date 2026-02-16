package com.sigret.dtos.presupuesto;

import com.sigret.enums.EstadoPresupuesto;
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
public class PresupuestoUpdateDto {

    private String diagnostico;

    private List<DetallePresupuestoDto> detalles;

    private BigDecimal manoObra;

    private BigDecimal montoTotal;

    private LocalDate fechaVencimiento;

    private EstadoPresupuesto estado;

    private String observaciones;
}
