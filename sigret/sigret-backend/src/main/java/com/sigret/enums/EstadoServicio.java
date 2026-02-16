package com.sigret.enums;

import lombok.Getter;

@Getter
public enum EstadoServicio {
    RECIBIDO("Recibido"),
    ESPERANDO_EVALUACION_GARANTIA("Esperando Evaluación Garantía"),
    PRESUPUESTADO("Presupuestado"),
    APROBADO("Aprobado"),
    EN_REPARACION("En Reparación"),
    TERMINADO("Terminado"),
    RECHAZADO("Rechazado"),
    GARANTIA_SIN_REPARACION("Garantía Sin Reparación"),
    GARANTIA_RECHAZADA("Garantía Rechazada"),
    FINALIZADO("Finalizado");

    private final String descripcion;

    EstadoServicio(String descripcion) {
        this.descripcion = descripcion;
    }

}
