package com.sigret.dtos.servicio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para representar un item usado en la orden de trabajo del servicio original
 * Se usa para mostrar en la evaluación de garantía
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemServicioOriginalDto {

    private Long repuestoId; // ID del repuesto si existe (null si es texto libre)
    private String item;
    private Integer cantidad;
    private String comentario; // comentario original del item
}
