package com.sigret.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_servicio")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @NotBlank(message = "El componente es obligatorio")
    @Size(max = 100, message = "El componente no puede exceder 100 caracteres")
    @Column(name = "componente", nullable = false, length = 100)
    private String componente;

    @NotNull(message = "Debe indicar si el componente está presente")
    @Column(name = "presente", nullable = false)
    private Boolean presente;

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;


    // utilidad
    public String getEstadoComponente() {
        return presente ? "Presente" : "Faltante";
    }
}
