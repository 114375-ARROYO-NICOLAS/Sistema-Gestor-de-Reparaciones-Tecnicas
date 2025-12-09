package com.sigret.dtos.presupuesto;

import com.sigret.enums.EstadoPresupuesto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoCreateDto {

    @NotNull(message = "El servicio es obligatorio")
    private Long servicioId;

    @NotNull(message = "El empleado es obligatorio")
    private Long empleadoId;

    private String numeroPresupuesto;

    private String diagnostico;

    @Valid
    private List<DetallePresupuestoDto> detalles = new ArrayList<>();

    private BigDecimal manoObra = BigDecimal.ZERO;

    private BigDecimal montoRepuestosOriginal = BigDecimal.ZERO;

    private BigDecimal montoRepuestosAlternativo;

    private BigDecimal montoTotalOriginal = BigDecimal.ZERO;

    private BigDecimal montoTotalAlternativo;

    private Boolean mostrarOriginal = true;

    private Boolean mostrarAlternativo = false;

    private LocalDate fechaVencimiento;

    private LocalDate fechaSolicitud;

    private LocalDate fechaPactada;

    private EstadoPresupuesto estado = EstadoPresupuesto.PENDIENTE;

    private String observaciones;
}
