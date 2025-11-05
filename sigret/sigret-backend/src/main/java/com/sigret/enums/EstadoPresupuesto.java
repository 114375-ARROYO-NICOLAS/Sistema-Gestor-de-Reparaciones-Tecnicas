package com.sigret.enums;

public enum EstadoPresupuesto {

    PENDIENTE("Pendiente"),
    EN_CURSO("En Curso"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado");

    private final String descripcion;

    EstadoPresupuesto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
