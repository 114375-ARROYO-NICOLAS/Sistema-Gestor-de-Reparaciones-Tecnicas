package com.sigret.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipos_equipo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoEquipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_equipo")
    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 100, message = "La descripción no puede exceder 100 caracteres")
    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

    @OneToMany(mappedBy = "tipoEquipo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Equipo> equipos = new ArrayList<>();

    @OneToMany(mappedBy = "tipoEquipo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Repuesto> repuestos = new ArrayList<>();

}
