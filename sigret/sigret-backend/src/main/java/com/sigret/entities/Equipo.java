package com.sigret.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name = "equipos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_equipo", nullable = false)
    private TipoEquipo tipoEquipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca", nullable = false)
    private Marca marca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modelo")
    private Modelo modelo;

    @Size(max = 50, message = "El número de serie no puede exceder 50 caracteres")
    @Column(name = "num_serie", length = 50)
    private String numeroSerie;

    @Size(max = 30, message = "El color no puede exceder 30 caracteres")
    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Relaciones inversas
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClienteEquipo> clienteEquipos = new ArrayList<>();

    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Servicio> servicios = new ArrayList<>();


    // Métodos de utilidad
    public String getDescripcionCompleta() {
        StringBuilder desc = new StringBuilder();
        if (tipoEquipo != null) desc.append(tipoEquipo.getDescripcion()).append(" ");
        if (marca != null) desc.append(marca.getDescripcion()).append(" ");
        if (modelo != null) desc.append(modelo.getDescripcion());
        return desc.toString().trim();
    }

    public String getIdentificacion() {
        if (numeroSerie != null && !numeroSerie.trim().isEmpty()) {
            return "S/N: " + numeroSerie;
        }
        return getDescripcionCompleta();
    }
}
