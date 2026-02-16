package com.sigret.dtos.detalleOrdenTrabajo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleOrdenTrabajoDto {

    private Long id;
    private String item;
    private Integer cantidad;
    private String comentario;
    private Boolean completado;
}
