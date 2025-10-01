package com.sigret.dtos.cliente;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponseDto {

    private Long id;
    private String nombreCompleto;
    private String documento;
    private String tipoPersona;
    private String tipoDocumento;
    private String email;
    private String telefono;
    private String comentarios;
    private Boolean esPersonaJuridica;
}
