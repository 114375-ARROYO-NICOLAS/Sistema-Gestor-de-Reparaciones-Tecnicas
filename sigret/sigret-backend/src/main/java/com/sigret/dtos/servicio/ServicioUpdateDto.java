package com.sigret.dtos.servicio;

import com.sigret.enums.EstadoServicio;
import com.sigret.enums.TipoIngreso;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioUpdateDto {

    private TipoIngreso tipoIngreso;

    private String firmaIngreso; // Base64 de la firma

    private String firmaConformidad; // Base64 de la firma

    private String fallaReportada; // Problema/falla reportada por el cliente

    private String observaciones; // Observaciones del técnico/empleado

    // Campos de garantía
    private Boolean esGarantia;

    private Long servicioGarantiaId;

    private Boolean garantiaDentroPlazo;

    private Boolean garantiaCumpleCondiciones;

    private String observacionesGarantia;

    private Long tecnicoEvaluacionId;

    private String observacionesEvaluacionGarantia;

    // Lista de items (repuestos) seleccionados en la evaluación de garantía
    // Solo se usa cuando garantiaCumpleCondiciones = true
    private List<ItemEvaluacionGarantiaDto> itemsEvaluacionGarantia;

    // Campos existentes
    private Boolean abonaVisita;

    private BigDecimal montoVisita;

    private BigDecimal montoPagado;

    private EstadoServicio estado;

    private LocalDate fechaDevolucionPrevista;

    private LocalDate fechaDevolucionReal;
}
