package com.sigret.dtos.presupuesto;

import com.sigret.enums.EstadoPresupuesto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoEventDto {
    private String tipoEvento;  // CREADO, ACTUALIZADO, CAMBIO_ESTADO, ELIMINADO
    private Long presupuestoId;
    private Long servicioId;
    private String numeroServicio;
    private EstadoPresupuesto estadoAnterior;
    private EstadoPresupuesto estadoNuevo;
    private PresupuestoListDto presupuesto;
}
