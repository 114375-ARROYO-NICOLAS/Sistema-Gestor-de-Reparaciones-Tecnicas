package com.sigret.dtos.tipoDocumento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumentoCreateDto {

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 30, message = "La descripción no puede exceder 30 caracteres")
    private String descripcion;
}

