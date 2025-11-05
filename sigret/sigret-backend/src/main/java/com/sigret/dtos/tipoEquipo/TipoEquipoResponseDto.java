package com.sigret.dtos.tipoEquipo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoEquipoResponseDto {

    private Long id;
    private String descripcion;
}
