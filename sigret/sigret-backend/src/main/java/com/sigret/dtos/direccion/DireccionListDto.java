package com.sigret.dtos.direccion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DireccionListDto {

    private Long id;
    private String placeId;
    private String calle;
    private String numero;
    private String ciudad;
    private String provincia;
    private String pais;
    private Boolean esPrincipal;
    private String direccionCompleta;
    private Double latitud;
    private Double longitud;
}

