package com.sigret.dtos.presupuesto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class PresupuestoActualizarReenviarDto {

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    @Valid
    private List<DetallePresupuestoDto> detalles;

    @DecimalMin(value = "0.0", message = "La mano de obra no puede ser negativa")
    private BigDecimal manoObra;

    private Boolean mostrarOriginal;

    private Boolean mostrarAlternativo;

    private Boolean reenviarEmail;

    private String mensajeAdicional;
}
