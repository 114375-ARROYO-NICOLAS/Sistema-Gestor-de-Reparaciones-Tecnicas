package com.sigret.enums;


import lombok.Getter;

@Getter
public enum RolUsuario {
    PROPIETARIO("Propietario"),
    ADMINISTRATIVO("Administrativo"),
    TECNICO("Técnico");

    private final String descripcion;

    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

}