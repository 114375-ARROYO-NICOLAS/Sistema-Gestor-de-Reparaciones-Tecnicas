package com.sigret.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "presupuesto_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_presupuesto_token")
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_presupuesto", nullable = false)
    private Presupuesto presupuesto;

    @Column(name = "tipo_accion", nullable = false, length = 20)
    private String tipoAccion; // "APROBAR" o "RECHAZAR"

    @Column(name = "tipo_precio", length = 20)
    private String tipoPrecio; // "ORIGINAL" o "ALTERNATIVO" (solo para APROBAR)

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private Boolean usado = false;

    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;

    @Column(name = "ip_uso", length = 45)
    private String ipUso;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}