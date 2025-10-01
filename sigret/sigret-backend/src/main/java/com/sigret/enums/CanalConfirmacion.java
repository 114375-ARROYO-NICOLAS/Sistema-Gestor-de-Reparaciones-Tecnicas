package com.sigret.enums;

public enum CanalConfirmacion {
    WHATSAPP("WhatsApp"),
    TELEFONO("Teléfono"),
    PRESENCIAL("Presencial"),
    EMAIL("Email");

    private final String descripcion;

    CanalConfirmacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
