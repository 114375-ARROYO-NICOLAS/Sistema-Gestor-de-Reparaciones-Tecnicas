package com.sigret.dtos.modelo;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModeloUpdateDto {

    @Size(max = 100, message = "La descripción no puede exceder 100 caracteres")
    private String descripcion;

    private Long marcaId;
}
