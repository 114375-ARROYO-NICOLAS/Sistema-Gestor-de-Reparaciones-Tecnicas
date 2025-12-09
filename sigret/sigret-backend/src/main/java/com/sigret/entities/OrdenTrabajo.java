package com.sigret.entities;

import com.sigret.enums.EstadoOrdenTrabajo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordenes_trabajo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_trabajo")
    private Long id;

    @Column(name = "numero_orden_trabajo", unique = true, nullable = false, length = 20)
    private String numeroOrdenTrabajo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_presupuesto")
    private Presupuesto presupuesto; // Null para garantías sin presupuesto

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    // CAMPOS EXISTENTES Y NUEVOS
    @Column(name = "monto_total_repuestos", precision = 10, scale = 2)
    private BigDecimal montoTotalRepuestos = BigDecimal.ZERO;

    @Column(name = "monto_extras", precision = 10, scale = 2)
    private BigDecimal montoExtras = BigDecimal.ZERO;

    @Column(name = "observaciones_extras", columnDefinition = "TEXT")
    private String observacionesExtras;

    // CAMPO PARA ÓRDENES DE GARANTÍA
    @Column(name = "es_sin_costo", nullable = false)
    private Boolean esSinCosto = false; // True para garantías

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoOrdenTrabajo estado = EstadoOrdenTrabajo.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_comienzo")
    private LocalDate fechaComienzo;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    // RELACIONES INVERSAS
    @OneToMany(mappedBy = "ordenTrabajo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleOrdenTrabajo> detalleOrdenesTrabajo = new ArrayList<>();

    // MÉTODOS DE UTILIDAD
    public BigDecimal getMontoTotalFinal() {
        if (this.esSinCosto) return BigDecimal.ZERO;
        return this.montoTotalRepuestos.add(this.montoExtras);
    }

    public long getDiasReparacion() {
        if (this.fechaComienzo == null || this.fechaFin == null) return 0;
        return ChronoUnit.DAYS.between(this.fechaComienzo, this.fechaFin);
    }

    public boolean tieneExtras() {
        return this.montoExtras != null && this.montoExtras.compareTo(BigDecimal.ZERO) > 0;
    }
}
