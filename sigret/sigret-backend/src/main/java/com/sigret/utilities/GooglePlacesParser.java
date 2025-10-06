package com.sigret.utilities;

import com.sigret.dtos.direccion.GooglePlacesDto;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para procesar y extraer información de Google Places API
 */
public class GooglePlacesParser {

    /**
     * Extrae los componentes de dirección del objeto Google Places
     * 
     * @param googlePlacesData Datos de Google Places API
     * @return Map con los componentes extraídos
     */
    public static Map<String, String> extractAddressComponents(GooglePlacesDto googlePlacesData) {
        Map<String, String> components = new HashMap<>();
        
        if (googlePlacesData == null || googlePlacesData.getAddressComponents() == null) {
            return components;
        }
        
        for (GooglePlacesDto.AddressComponentDto component : googlePlacesData.getAddressComponents()) {
            if (component.getTypes() == null) continue;
            
            // Número de calle
            if (component.getTypes().contains("street_number")) {
                components.put("numero", component.getLongName());
            }
            
            // Nombre de la calle/ruta
            if (component.getTypes().contains("route")) {
                components.put("calle", component.getLongName());
            }
            
            // Barrio/Vecindario
            if (component.getTypes().contains("sublocality") || 
                component.getTypes().contains("sublocality_level_1") ||
                component.getTypes().contains("neighborhood")) {
                components.put("barrio", component.getLongName());
            }
            
            // Ciudad/Localidad
            if (component.getTypes().contains("locality")) {
                components.put("ciudad", component.getLongName());
            }
            
            // Partido/Departamento (si no hay locality, usar administrative_area_level_2)
            if (component.getTypes().contains("administrative_area_level_2") && !components.containsKey("ciudad")) {
                components.put("ciudad", component.getLongName());
            }
            
            // Provincia/Estado
            if (component.getTypes().contains("administrative_area_level_1")) {
                components.put("provincia", component.getLongName());
            }
            
            // País
            if (component.getTypes().contains("country")) {
                components.put("pais", component.getLongName());
            }
            
            // Código postal
            if (component.getTypes().contains("postal_code")) {
                components.put("codigoPostal", component.getLongName());
            }
        }
        
        return components;
    }
    
    /**
     * Extrae las coordenadas geográficas del objeto Google Places
     * 
     * @param googlePlacesData Datos de Google Places API
     * @return Array con [latitud, longitud] o null si no hay datos
     */
    public static Double[] extractCoordinates(GooglePlacesDto googlePlacesData) {
        if (googlePlacesData == null || 
            googlePlacesData.getGeometry() == null || 
            googlePlacesData.getGeometry().getLocation() == null) {
            return null;
        }
        
        GooglePlacesDto.LocationDto location = googlePlacesData.getGeometry().getLocation();
        return new Double[]{location.getLat(), location.getLng()};
    }
}

