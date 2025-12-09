package com.sigret.dtos.ordenTrabajo;

import com.sigret.dtos.detalleOrdenTrabajo.DetalleOrdenTrabajoDto;
import com.sigret.enums.EstadoOrdenTrabajo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajoResponseDto {

    private Long id;
    private String numeroOrdenTrabajo;
    private Long servicioId;
    private String numeroServicio;
    private String clienteNombre;
    private String equipoDescripcion;
    private Long presupuestoId;
    private String numeroPresupuesto;
    private Long empleadoId;
    private String empleadoNombre;
    private BigDecimal montoTotalRepuestos;
    private BigDecimal montoExtras;
    private String observacionesExtras;
    private Boolean esSinCosto;
    private EstadoOrdenTrabajo estado;
    private LocalDateTime fechaCreacion;
    private LocalDate fechaComienzo;
    private LocalDate fechaFin;
    private BigDecimal montoTotalFinal;
    private Long diasReparacion;
    private List<DetalleOrdenTrabajoDto> detalles = new ArrayList<>();
}
