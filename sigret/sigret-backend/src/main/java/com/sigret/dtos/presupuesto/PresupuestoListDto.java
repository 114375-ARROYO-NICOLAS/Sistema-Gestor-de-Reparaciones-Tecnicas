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
public class PresupuestoListDto {

    private Long id;
    private String numeroPresupuesto;
    private String numeroServicio;
    private String clienteNombre;
    private String empleadoNombre;  // El empleado que hace el presupuesto
    private String equipoDescripcion;  // Para mostrar qué equipo es
    private BigDecimal montoTotal;
    private LocalDate fechaVencimiento;
    private EstadoPresupuesto estado;
    private LocalDateTime fechaCreacion;
}
