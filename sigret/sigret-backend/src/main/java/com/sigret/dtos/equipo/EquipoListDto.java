package com.sigret.dtos.equipo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipoListDto {

    private Long id;
    private String descripcionCompleta;
    private String numeroSerie;
    private String color;
    private String tipoEquipo;
    private String marca;
    private String modelo;
}
