package com.sigret.enums;

public enum TipoConfirmacion {
    ORIGINAL("Original"),
    ALTERNATIVO("Alternativo");

    private final String descripcion;

    TipoConfirmacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
