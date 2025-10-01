package com.sigret.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empleados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_empleado", nullable = false)
    private TipoEmpleado tipoEmpleado;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Relaciones inversas
    @OneToMany(mappedBy = "empleadoRecepcion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Servicio> serviciosRecepcion = new ArrayList<>();

    @OneToOne(mappedBy = "empleado", cascade = CascadeType.ALL)
    private Usuario usuario;


    // Métodos de utilidad
    public String getNombreCompleto() {
        return persona != null ? persona.getNombreCompleto() : "";
    }

    public boolean puedeLoguear() {
        return activo && usuario != null && usuario.getActivo();
    }
}
