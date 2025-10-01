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
@Table(name = "tipos_contacto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoContacto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_contacto")
    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 50, message = "La descripción no puede exceder 50 caracteres")
    @Column(name = "descripcion", nullable = false, length = 50)
    private String descripcion;

    @OneToMany(mappedBy = "tipoContacto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contacto> contactos = new ArrayList<>();

}
