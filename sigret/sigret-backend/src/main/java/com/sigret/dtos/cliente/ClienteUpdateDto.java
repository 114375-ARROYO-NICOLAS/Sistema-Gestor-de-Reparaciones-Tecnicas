package com.sigret.dtos.cliente;

import com.sigret.dtos.contacto.ContactoCreateDto;
import com.sigret.dtos.direccion.DireccionCreateDto;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteUpdateDto {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @Size(max = 200, message = "La razón social no puede exceder 200 caracteres")
    private String razonSocial;

    @Size(max = 1, message = "El sexo debe ser un solo carácter")
    private String sexo;

    @Size(max = 1000, message = "Los comentarios no pueden exceder 1000 caracteres")
    private String comentarios;
    
    // Contactos (opcional) - si se envía, reemplaza todos los contactos existentes
    private List<ContactoCreateDto> contactos;
    
    // Direcciones (opcional) - si se envía, reemplaza todas las direcciones existentes
    private List<DireccionCreateDto> direcciones;
}
