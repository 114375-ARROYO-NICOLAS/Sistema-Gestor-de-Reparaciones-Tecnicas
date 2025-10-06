package com.sigret.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "direcciones")
@Getter
@Setter
@NoArgsConstructor
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    // Campos de Google Places API
    @Size(max = 255, message = "El Place ID no puede exceder 255 caracteres")
    @Column(name = "place_id", length = 255, unique = true)
    private String placeId;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    @Size(max = 500, message = "La dirección formateada no puede exceder 500 caracteres")
    @Column(name = "direccion_formateada", length = 500)
    private String direccionFormateada;

    // Campos de dirección estructurados
    @Size(max = 200, message = "La calle no puede exceder 200 caracteres")
    @Column(name = "calle", length = 200)
    private String calle;

    @Size(max = 20, message = "El número no puede exceder 20 caracteres")
    @Column(name = "numero", length = 20)
    private String numero;

    @Size(max = 10, message = "El piso no puede exceder 10 caracteres")
    @Column(name = "piso", length = 10)
    private String piso;

    @Size(max = 10, message = "El departamento no puede exceder 10 caracteres")
    @Column(name = "departamento", length = 10)
    private String departamento;

    @Size(max = 200, message = "El barrio no puede exceder 200 caracteres")
    @Column(name = "barrio", length = 200)
    private String barrio;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Size(max = 100, message = "La provincia no puede exceder 100 caracteres")
    @Column(name = "provincia", length = 100)
    private String provincia;

    @Size(max = 20, message = "El código postal no puede exceder 20 caracteres")
    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    @Column(name = "pais", length = 100)
    private String pais;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = false;

    // Métodos de utilidad
    public String getDireccionCompleta() {
        // Si existe la dirección formateada de Google Places, usarla
        if (direccionFormateada != null && !direccionFormateada.trim().isEmpty()) {
            StringBuilder sb = new StringBuilder(direccionFormateada);
            
            // Agregar piso y departamento si existen (Google Places no los incluye)
            if (piso != null && !piso.trim().isEmpty()) {
                sb.append(", Piso ").append(piso);
            }
            
            if (departamento != null && !departamento.trim().isEmpty()) {
                sb.append(", Depto. ").append(departamento);
            }
            
            return sb.toString();
        }
        
        // Si no, construir manualmente
        StringBuilder sb = new StringBuilder();
        
        if (calle != null && numero != null) {
            sb.append(calle).append(" ").append(numero);
        } else if (calle != null) {
            sb.append(calle);
        }
        
        if (piso != null && !piso.trim().isEmpty()) {
            sb.append(", Piso ").append(piso);
        }
        
        if (departamento != null && !departamento.trim().isEmpty()) {
            sb.append(", Depto. ").append(departamento);
        }
        
        if (barrio != null && !barrio.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(barrio);
        }
        
        if (ciudad != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ciudad);
        }
        
        if (provincia != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(provincia);
        }
        
        if (codigoPostal != null && !codigoPostal.trim().isEmpty()) {
            sb.append(" (").append(codigoPostal).append(")");
        }
        
        if (pais != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(pais);
        }
        
        return sb.toString();
    }
    
    public boolean tieneUbicacion() {
        return latitud != null && longitud != null;
    }
}

