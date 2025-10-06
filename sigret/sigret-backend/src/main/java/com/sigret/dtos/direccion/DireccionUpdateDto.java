package com.sigret.dtos.direccion;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DireccionUpdateDto {

    // Datos de Google Places API
    @Size(max = 255, message = "El Place ID no puede exceder 255 caracteres")
    private String placeId;

    private Double latitud;

    private Double longitud;

    @Size(max = 500, message = "La dirección formateada no puede exceder 500 caracteres")
    private String direccionFormateada;

    // Campos estructurados
    @Size(max = 200, message = "La calle no puede exceder 200 caracteres")
    private String calle;

    @Size(max = 20, message = "El número no puede exceder 20 caracteres")
    private String numero;

    @Size(max = 10, message = "El piso no puede exceder 10 caracteres")
    private String piso;

    @Size(max = 10, message = "El departamento no puede exceder 10 caracteres")
    private String departamento;

    @Size(max = 200, message = "El barrio no puede exceder 200 caracteres")
    private String barrio;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;

    @Size(max = 100, message = "La provincia no puede exceder 100 caracteres")
    private String provincia;

    @Size(max = 20, message = "El código postal no puede exceder 20 caracteres")
    private String codigoPostal;

    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String pais;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    private Boolean esPrincipal;

    // Objeto completo de Google Places (opcional)
    private GooglePlacesDto googlePlacesData;
}

