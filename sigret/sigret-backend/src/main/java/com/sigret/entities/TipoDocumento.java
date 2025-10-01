package com.sigret.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipos_documento")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TipoDocumento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_documento")
    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 30, message = "La descripción no puede exceder 30 caracteres")
    @Column(name = "descripcion", nullable = false, length = 30)
    private String descripcion;

    @OneToMany(mappedBy = "tipoDocumento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Persona> personas = new ArrayList<>();

}
