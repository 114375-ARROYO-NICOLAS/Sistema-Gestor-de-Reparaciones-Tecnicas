package com.sigret.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_ordenes_trabajo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleOrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_orden_trabajo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    // Para ordenes con repuesto específico (ej: garantías que usan repuestos del catálogo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_repuesto")
    private Repuesto repuesto;

    // Para ordenes con item de texto libre (ej: desde presupuesto)
    @Column(name = "item_descripcion", length = 200)
    private String itemDescripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "completado", nullable = false)
    private Boolean completado = false;

    // Método de utilidad para obtener la descripción del item
    public String getItemDisplay() {
        if (repuesto != null) {
            return repuesto.getDescripcionCompleta();
        }
        return itemDescripcion != null ? itemDescripcion : "";
    }

}