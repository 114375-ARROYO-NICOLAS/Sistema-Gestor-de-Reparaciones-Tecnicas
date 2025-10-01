package com.sigret.dtos.presupuesto;

import com.sigret.enums.EstadoPresupuesto;
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
public class PresupuestoResponseDto {

    private Long id;
    private String numeroPresupuesto;
    private Long servicioId;
    private String numeroServicio;
    private String clienteNombre;
    private String equipoDescripcion;
    private BigDecimal montoTotal;
    private LocalDate fechaVencimiento;
    private EstadoPresupuesto estado;
    private String observaciones;
    private LocalDateTime fechaCreacion;
}
