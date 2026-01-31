package com.sigret.dtos.repuesto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepuestoUpdateDto {

    private Long tipoEquipoId;

    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;
}
