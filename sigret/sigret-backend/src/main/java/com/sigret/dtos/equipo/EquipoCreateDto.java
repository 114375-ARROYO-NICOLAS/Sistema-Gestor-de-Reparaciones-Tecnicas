package com.sigret.dtos.equipo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipoCreateDto {

    @NotNull(message = "El tipo de equipo es obligatorio")
    private Long tipoEquipoId;

    @NotNull(message = "La marca es obligatoria")
    private Long marcaId;

    private Long modeloId;

    @Size(max = 50, message = "El número de serie no puede exceder 50 caracteres")
    private String numeroSerie;

    @Size(max = 30, message = "El color no puede exceder 30 caracteres")
    private String color;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
}
