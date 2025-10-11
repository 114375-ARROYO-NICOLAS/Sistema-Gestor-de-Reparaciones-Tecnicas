package com.sigret.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Relaciones inversas
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClienteEquipo> clienteEquipos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Servicio> servicios = new ArrayList<>();


    public Cliente(Persona persona) {
        this.persona = persona;
    }


    // Métodos de utilidad
    public String getNombreCompleto() {
        return persona != null ? persona.getNombreCompleto() : "";
    }

    public String getDocumento() {
        return persona != null ? persona.getDocumento() : "";
    }

    public boolean esPersonaJuridica() {
        return persona != null && persona.esPersonaJuridica();
    }

    public String getPrimerEmail() {
        if (persona != null && persona.getContactos() != null) {
            return persona.getContactos().stream()
                    .filter(c -> "Email".equalsIgnoreCase(c.getTipoContacto().getDescripcion()))
                    .map(Contacto::getDescripcion)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public String getPrimerTelefono() {
        if (persona != null && persona.getContactos() != null) {
            return persona.getContactos().stream()
                    .filter(c -> c.getTipoContacto().getDescripcion().toLowerCase().contains("teléfono")
                            || c.getTipoContacto().getDescripcion().toLowerCase().contains("celular"))
                    .map(Contacto::getDescripcion)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
