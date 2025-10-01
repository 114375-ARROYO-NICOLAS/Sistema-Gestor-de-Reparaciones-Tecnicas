package com.sigret.enums;

import lombok.Getter;

@Getter
public enum TipoIngreso {
    CLIENTE_TRAE("Cliente Trae Equipo"),
    EMPRESA_BUSCA("Empresa Busca Equipo");

    private final String descripcion;

    TipoIngreso(String descripcion) {
        this.descripcion = descripcion;
    }

}