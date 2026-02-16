package com.sigret.dtos.repuesto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepuestoDto {

    private Long id;

    private String descripcion;

    private String tipoEquipo;

    private String descripcionCompleta;
}
