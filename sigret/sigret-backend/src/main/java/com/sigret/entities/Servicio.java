package com.sigret.entities;

import com.sigret.enums.EstadoServicio;
import com.sigret.enums.TipoIngreso;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long id;

    @NotBlank(message = "El número de servicio es obligatorio")
    @Size(max = 20, message = "El número de servicio no puede exceder 20 caracteres")
    @Column(name = "numero_servicio", nullable = false, unique = true, length = 20)
    private String numeroServicio; // ej: SRV-2025-001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipo", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado_recepcion", nullable = false)
    private Empleado empleadoRecepcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ingreso", nullable = false)
    private TipoIngreso tipoIngreso;

    @Lob
    @Column(name = "firma_ingreso", columnDefinition = "LONGTEXT")
    private String firmaIngreso; // Base64 de la firma

    @Lob
    @Column(name = "firma_conformidad", columnDefinition = "LONGTEXT")
    private String firmaConformidad; // Base64 de la firma

    // CAMPOS DE GARANTÍA
    @Column(name = "es_garantia", nullable = false)
    private Boolean esGarantia = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio_garantia")
    private Servicio servicioGarantia; // Referencia al servicio original

    @Column(name = "garantia_dentro_plazo")
    private Boolean garantiaDentroPlazo;

    @Column(name = "garantia_cumple_condiciones")
    private Boolean garantiaCumpleCondiciones;

    @Column(name = "observaciones_garantia", columnDefinition = "TEXT")
    private String observacionesGarantia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tecnico_evaluacion")
    private Empleado tecnicoEvaluacion;

    @Column(name = "fecha_evaluacion_garantia")
    private LocalDateTime fechaEvaluacionGarantia;

    @Column(name = "observaciones_evaluacion_garantia", columnDefinition = "TEXT")
    private String observacionesEvaluacionGarantia;

    // CAMPOS EXISTENTES
    @Column(name = "abona_visita", nullable = false)
    private Boolean abonaVisita = false;

    @Column(name = "monto_visita", precision = 10, scale = 2)
    private BigDecimal montoVisita = BigDecimal.ZERO;

    @Column(name = "monto_pagado", precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoServicio estado = EstadoServicio.RECIBIDO;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDate fechaRecepcion = LocalDate.now();

    @Column(name = "fecha_devolucion_prevista")
    private LocalDate fechaDevolucionPrevista;

    @Column(name = "fecha_devolucion_real")
    private LocalDate fechaDevolucionReal;

    // RELACIONES INVERSAS
    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Presupuesto> presupuestos = new ArrayList<>();

    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrdenTrabajo> ordenesTrabajo = new ArrayList<>();

    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleServicio> detalleServicios = new ArrayList<>();

    @OneToMany(mappedBy = "servicioGarantia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Servicio> serviciosGarantiaAsociados = new ArrayList<>();


    // CONSTRUCTORES
    public Servicio(String numeroServicio, Cliente cliente, Equipo equipo,
                    Empleado empleadoRecepcion, TipoIngreso tipoIngreso) {
        this.numeroServicio = numeroServicio;
        this.cliente = cliente;
        this.equipo = equipo;
        this.empleadoRecepcion = empleadoRecepcion;
        this.tipoIngreso = tipoIngreso;
        this.estado = EstadoServicio.RECIBIDO;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaRecepcion = LocalDate.now();
        this.esGarantia = false;
    }

    // MÉTODOS DE UTILIDAD
    public boolean esVigentEnGarantia() {
        if (this.fechaDevolucionReal == null) return false;
        return this.fechaDevolucionReal.plusDays(90).isAfter(LocalDate.now());
    }

    public boolean puedeGenerarGarantia() {
        return this.estado == EstadoServicio.TERMINADO && this.esVigentEnGarantia();
    }

    public String getDescripcionCompleta() {
        return numeroServicio + " - " +
                (cliente != null ? cliente.getNombreCompleto() : "") + " - " +
                (equipo != null ? equipo.getDescripcionCompleta() : "");
    }
}
