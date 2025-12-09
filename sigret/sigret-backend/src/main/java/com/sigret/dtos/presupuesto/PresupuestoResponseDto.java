package com.sigret.dtos.presupuesto;

import com.sigret.enums.CanalConfirmacion;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.enums.TipoConfirmacion;
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
public class PresupuestoResponseDto {

    private Long id;
    private String numeroPresupuesto;
    private Long servicioId;
    private String numeroServicio;
    private String clienteNombre;
    private String equipoDescripcion;
    private Long empleadoId;
    private String empleadoNombre;
    private String problema;
    private String diagnostico;
    private List<DetallePresupuestoDto> detalles = new ArrayList<>();
    private BigDecimal manoObra;
    private BigDecimal montoRepuestosOriginal;
    private BigDecimal montoRepuestosAlternativo;
    private BigDecimal montoTotalOriginal;
    private BigDecimal montoTotalAlternativo;
    private Boolean mostrarOriginal;
    private Boolean mostrarAlternativo;
    private TipoConfirmacion tipoConfirmado;
    private LocalDateTime fechaConfirmacion;
    private CanalConfirmacion canalConfirmacion;
    private LocalDate fechaVencimiento;
    private LocalDate fechaSolicitud;
    private LocalDate fechaPactada;
    private EstadoPresupuesto estado;
    private String observaciones;
    private LocalDateTime fechaCreacion;
}
