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
@Table(name = "repuestos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Repuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_repuesto")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_equipo", nullable = false)
    private TipoEquipo tipoEquipo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    // Relación inversa
    @OneToMany(mappedBy = "repuesto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleOrdenTrabajo> detalleOrdenes = new ArrayList<>();


    // utilidad
    public String getDescripcionCompleta() {
        return tipoEquipo != null ?
                tipoEquipo.getDescripcion() + " - " + descripcion :
                descripcion;
    }
}
