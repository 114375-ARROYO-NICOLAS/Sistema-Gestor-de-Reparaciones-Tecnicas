package com.sigret.dtos.ordenTrabajo;

import com.sigret.enums.EstadoOrdenTrabajo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajoEventDto {
    private String tipoEvento;  // CREADO, ACTUALIZADO, CAMBIO_ESTADO, ELIMINADO
    private Long ordenTrabajoId;
    private Long servicioId;
    private String numeroServicio;
    private EstadoOrdenTrabajo estadoAnterior;
    private EstadoOrdenTrabajo estadoNuevo;
    private OrdenTrabajoListDto ordenTrabajo;
}
