package com.sigret.dtos.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModeloListDto {

    private Long id;
    private String descripcion;
    private Long marcaId;
    private String marcaDescripcion;
}
