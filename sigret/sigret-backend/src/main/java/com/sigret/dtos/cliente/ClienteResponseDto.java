package com.sigret.dtos.cliente;

import com.sigret.dtos.contacto.ContactoListDto;
import com.sigret.dtos.direccion.DireccionListDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
    private Boolean activo;

    // Contactos de la persona
    private List<ContactoListDto> contactos;

    // Direcciones de la persona
    private List<DireccionListDto> direcciones;
}
