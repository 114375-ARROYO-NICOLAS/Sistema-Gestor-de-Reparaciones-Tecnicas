package com.sigret.enums;

public enum EstadoOrdenTrabajo {
    PENDIENTE("Pendiente"),
    EN_PROGRESO("En Progreso"),
    TERMINADA("Terminada"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoOrdenTrabajo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
