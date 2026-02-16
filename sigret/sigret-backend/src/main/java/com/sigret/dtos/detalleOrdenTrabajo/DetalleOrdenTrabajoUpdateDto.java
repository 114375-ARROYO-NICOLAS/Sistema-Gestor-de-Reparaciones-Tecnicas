package com.sigret.dtos.detalleOrdenTrabajo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleOrdenTrabajoUpdateDto {

    private String comentario;
    private Boolean completado;
}
