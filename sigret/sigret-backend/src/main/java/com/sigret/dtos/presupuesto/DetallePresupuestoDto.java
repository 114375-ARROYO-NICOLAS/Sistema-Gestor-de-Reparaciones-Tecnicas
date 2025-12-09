package com.sigret.dtos.presupuesto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetallePresupuestoDto {

    private Long id;

    @NotBlank(message = "El ítem es obligatorio")
    @Size(max = 200, message = "El ítem no puede exceder 200 caracteres")
    private String item;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El precio original es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precioOriginal;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio alternativo debe ser mayor a 0")
    private BigDecimal precioAlternativo;
}

