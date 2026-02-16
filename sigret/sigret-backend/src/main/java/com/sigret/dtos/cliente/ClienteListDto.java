package com.sigret.dtos.cliente;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteListDto {

    private Long id;
    private String nombreCompleto;
    private String documento;
    private String email;
    private String telefono;
    private String direccionPrincipal;
    private Boolean esPersonaJuridica;
    private Boolean activo;
}
