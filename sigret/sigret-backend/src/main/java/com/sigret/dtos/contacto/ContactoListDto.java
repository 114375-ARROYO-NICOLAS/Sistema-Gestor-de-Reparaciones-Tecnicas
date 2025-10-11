package com.sigret.dtos.contacto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactoListDto {
    
    private Long id;
    private String tipoContacto;
    private String descripcion;
}

