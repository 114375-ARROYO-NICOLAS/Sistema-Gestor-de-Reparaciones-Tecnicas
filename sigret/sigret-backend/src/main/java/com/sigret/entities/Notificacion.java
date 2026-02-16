package com.sigret.entities;

import com.sigret.enums.TipoReferencia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones", indexes = {
    @Index(name = "idx_notif_usuario", columnList = "id_usuario"),
    @Index(name = "idx_notif_usuario_leida", columnList = "id_usuario, leida"),
    @Index(name = "idx_notif_fecha", columnList = "fecha_creacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long id;

    @Column(name = "mensaje", columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;

    @Column(name = "leida", nullable = false)
    private Boolean leida = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_referencia", length = 30)
    private TipoReferencia tipoReferencia;

    @Column(name = "icono", length = 50)
    private String icono;

    @Column(name = "severidad", length = 20)
    private String severidad;
}
