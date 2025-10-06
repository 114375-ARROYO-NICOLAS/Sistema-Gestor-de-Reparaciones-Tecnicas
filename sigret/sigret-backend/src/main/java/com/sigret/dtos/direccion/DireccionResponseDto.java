package com.sigret.dtos.direccion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DireccionResponseDto {

    private Long id;
    private Long personaId;
    private String nombrePersona;
    
    // Datos de Google Places
    private String placeId;
    private Double latitud;
    private Double longitud;
    private String direccionFormateada;
    
    // Campos estructurados
    private String calle;
    private String numero;
    private String piso;
    private String departamento;
    private String barrio;
    private String ciudad;
    private String provincia;
    private String codigoPostal;
    private String pais;
    private String observaciones;
    private Boolean esPrincipal;
    private String direccionCompleta;
    private Boolean tieneUbicacion;
}

