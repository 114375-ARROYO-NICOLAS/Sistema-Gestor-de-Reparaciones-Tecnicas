package com.sigret.dtos.cliente;

import com.sigret.entities.TipoDocumento;
import com.sigret.entities.TipoPersona;
import jakarta.validation.constraints.NotBlank;
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
public class ClienteCreateDto {

    @NotNull(message = "El tipo de persona es obligatorio")
    private TipoPersona tipoPersona;

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @Size(max = 200, message = "La razón social no puede exceder 200 caracteres")
    private String razonSocial;

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El documento es obligatorio")
    @Size(max = 20, message = "El documento no puede exceder 20 caracteres")
    private String documento;

    @Size(max = 1, message = "El sexo debe ser un solo carácter")
    private String sexo;

    @Size(max = 1000, message = "Los comentarios no pueden exceder 1000 caracteres")
    private String comentarios;
}
