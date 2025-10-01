package com.sigret.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_presupuestos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetallePresupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_presupuesto")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_presupuesto", nullable = false)
    private Presupuesto presupuesto;

    @NotBlank(message = "El ítem es obligatorio")
    @Size(max = 200, message = "El ítem no puede exceder 200 caracteres")
    @Column(name = "item", nullable = false, length = 200)
    private String item;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    // PRECIOS DUALES POR ÍTEM
    @NotNull(message = "El precio original es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Column(name = "precio_original", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioOriginal;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio alternativo debe ser mayor a 0")
    @Column(name = "precio_alternativo", precision = 10, scale = 2)
    private BigDecimal precioAlternativo;

    // MÉTODOS DE UTILIDAD
    public BigDecimal getSubtotalOriginal() {
        return this.precioOriginal.multiply(BigDecimal.valueOf(this.cantidad));
    }

    public BigDecimal getSubtotalAlternativo() {
        if (this.precioAlternativo == null) return BigDecimal.ZERO;
        return this.precioAlternativo.multiply(BigDecimal.valueOf(this.cantidad));
    }

    public boolean tieneAlternativo() {
        return this.precioAlternativo != null &&
                this.precioAlternativo.compareTo(BigDecimal.ZERO) > 0;
    }
}
