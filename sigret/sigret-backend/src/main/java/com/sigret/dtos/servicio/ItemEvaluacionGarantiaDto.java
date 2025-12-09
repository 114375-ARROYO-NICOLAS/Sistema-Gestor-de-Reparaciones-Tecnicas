package com.sigret.dtos.servicio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para representar un item seleccionado en la evaluación de garantía
 * Solo contiene el ID del repuesto que presenta falla
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemEvaluacionGarantiaDto {

    private Long repuestoId; // ID del repuesto que presenta falla
    private String comentario; // comentario opcional sobre la falla
}