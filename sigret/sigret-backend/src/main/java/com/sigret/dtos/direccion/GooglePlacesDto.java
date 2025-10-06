package com.sigret.dtos.direccion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para recibir datos de Google Places API
 * Este DTO modela la estructura básica que devuelve Google Places
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GooglePlacesDto {

    private String placeId;
    private String formattedAddress;
    private GeometryDto geometry;
    private List<AddressComponentDto> addressComponents;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeometryDto {
        private LocationDto location;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDto {
        private Double lat;
        private Double lng;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressComponentDto {
        private String longName;
        private String shortName;
        private List<String> types;
    }
}

