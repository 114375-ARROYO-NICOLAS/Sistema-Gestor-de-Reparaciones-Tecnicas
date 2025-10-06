package com.sigret.dtos.tipoEmpleado;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoEmpleadoResponseDto {

    private Long id;
    private String descripcion;
}

