package com.sigret.dtos.detalleservicio;

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
public class DetalleServicioDto {

    private Long id;

    @NotBlank(message = "El componente es obligatorio")
    @Size(max = 100, message = "El componente no puede exceder 100 caracteres")
    private String componente;

    @NotNull(message = "Debe indicar si el componente está presente")
    private Boolean presente;

    private String comentario;
}
