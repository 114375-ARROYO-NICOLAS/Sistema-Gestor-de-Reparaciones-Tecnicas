package com.sigret.entities;

import com.sigret.enums.CanalConfirmacion;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.enums.TipoConfirmacion;
import jakarta.persistence.*;
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
@Table(name = "presupuestos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_presupuesto")
    private Long id;

    @Column(name = "numero_presupuesto", unique = true, nullable = false, length = 20)
    private String numeroPresupuesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    // CAMPOS PARA PRECIOS DUALES
    @Column(name = "monto_repuestos_original", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoRepuestosOriginal = BigDecimal.ZERO;

    @Column(name = "monto_repuestos_alternativo", precision = 10, scale = 2)
    private BigDecimal montoRepuestosAlternativo;

    @Column(name = "mano_obra", precision = 10, scale = 2, nullable = false)
    private BigDecimal manoObra = BigDecimal.ZERO;

    @Column(name = "monto_total_original", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoTotalOriginal = BigDecimal.ZERO;

    @Column(name = "monto_total_alternativo", precision = 10, scale = 2)
    private BigDecimal montoTotalAlternativo;

    // CAMPOS PARA CONFIGURAR QUÉ MOSTRAR
    @Column(name = "mostrar_original", nullable = false)
    private Boolean mostrarOriginal = true;

    @Column(name = "mostrar_alternativo", nullable = false)
    private Boolean mostrarAlternativo = false;

    // CAMPOS DE CONFIRMACIÓN
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_confirmado")
    private TipoConfirmacion tipoConfirmado;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_confirmacion")
    private CanalConfirmacion canalConfirmacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPresupuesto estado = EstadoPresupuesto.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud;

    @Column(name = "fecha_pactada")
    private LocalDate fechaPactada;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    // RELACIONES INVERSAS
    @OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetallePresupuesto> detallePresupuestos = new ArrayList<>();

    @OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrdenTrabajo> ordenesTrabajo = new ArrayList<>();

    // MÉTODOS DE UTILIDAD
    public void calcularTotales() {
        this.montoTotalOriginal = this.montoRepuestosOriginal.add(this.manoObra);
        if (this.montoRepuestosAlternativo != null) {
            this.montoTotalAlternativo = this.montoRepuestosAlternativo.add(this.manoObra);
        }
    }

    public void recalcularMontos() {
        // Recalcular montos de repuestos desde los detalles
        this.montoRepuestosOriginal = BigDecimal.ZERO;
        this.montoRepuestosAlternativo = null;

        boolean tieneAlternativos = false;
        BigDecimal sumaAlternativos = BigDecimal.ZERO;

        for (DetallePresupuesto detalle : this.detallePresupuestos) {
            // Sumar precios originales
            BigDecimal subtotalOriginal = detalle.getPrecioOriginal()
                    .multiply(BigDecimal.valueOf(detalle.getCantidad()));
            this.montoRepuestosOriginal = this.montoRepuestosOriginal.add(subtotalOriginal);

            // Sumar precios alternativos si existen
            if (detalle.getPrecioAlternativo() != null) {
                tieneAlternativos = true;
                BigDecimal subtotalAlternativo = detalle.getPrecioAlternativo()
                        .multiply(BigDecimal.valueOf(detalle.getCantidad()));
                sumaAlternativos = sumaAlternativos.add(subtotalAlternativo);
            }
        }

        if (tieneAlternativos) {
            this.montoRepuestosAlternativo = sumaAlternativos;
        }

        // Recalcular totales
        calcularTotales();
    }

    public List<DetallePresupuesto> getDetalles() {
        return this.detallePresupuestos;
    }

    public BigDecimal getMontoConfirmado() {
        if (this.tipoConfirmado == TipoConfirmacion.ORIGINAL) {
            return this.montoTotalOriginal;
        } else if (this.tipoConfirmado == TipoConfirmacion.ALTERNATIVO) {
            return this.montoTotalAlternativo;
        }
        return BigDecimal.ZERO;
    }

    public boolean tieneAlternativo() {
        return this.montoRepuestosAlternativo != null &&
                this.montoRepuestosAlternativo.compareTo(BigDecimal.ZERO) > 0;
    }
}
