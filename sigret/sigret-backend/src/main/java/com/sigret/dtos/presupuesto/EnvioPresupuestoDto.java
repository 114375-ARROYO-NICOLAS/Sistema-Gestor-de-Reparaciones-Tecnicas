package com.sigret.dtos.presupuesto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnvioPresupuestoDto {

    @NotNull(message = "El ID del presupuesto es requerido")
    private Long presupuestoId;

    @NotNull(message = "Debe seleccionar al menos un tipo de precio")
    private Boolean mostrarOriginal;

    @NotNull(message = "Debe seleccionar al menos un tipo de precio")
    private Boolean mostrarAlternativo;

    private String mensajeAdicional;

    @AssertTrue(message = "Debe mostrar al menos un tipo de precio")
    public boolean isAlMenosUnPrecioSeleccionado() {
        return Boolean.TRUE.equals(mostrarOriginal) || Boolean.TRUE.equals(mostrarAlternativo);
    }
}