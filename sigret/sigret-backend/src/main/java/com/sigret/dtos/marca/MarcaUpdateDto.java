package com.sigret.dtos.marca;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarcaUpdateDto {

    @Size(max = 100, message = "La descripción no puede exceder 100 caracteres")
    private String descripcion;
}
