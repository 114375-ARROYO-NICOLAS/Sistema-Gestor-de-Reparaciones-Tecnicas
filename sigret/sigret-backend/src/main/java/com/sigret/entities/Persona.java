package com.sigret.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personas")
@Getter
@Setter
@NoArgsConstructor
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_persona", nullable = false)
    private TipoPersona tipoPersona;

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(name = "nombre", length = 100)
    private String nombre;

    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    @Column(name = "apellido", length = 100)
    private String apellido;

    @Size(max = 200, message = "La razón social no puede exceder 200 caracteres")
    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Size(max = 20, message = "El documento no puede exceder 20 caracteres")
    @Column(name = "documento", length = 20, unique = true)
    private String documento;

    @Size(max = 1, message = "El sexo debe ser un solo carácter")
    @Column(name = "sexo", length = 1)
    private String sexo;

    // Relaciones inversas
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contacto> contactos = new ArrayList<>();

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Direccion> direcciones = new ArrayList<>();

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cliente cliente;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Empleado empleado;


    // Métodos de utilidad
    public String getNombreCompleto() {
        if (razonSocial != null && !razonSocial.trim().isEmpty()) {
            return razonSocial;
        }
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }

    public boolean esPersonaJuridica() {
        return razonSocial != null && !razonSocial.trim().isEmpty();
    }
}
