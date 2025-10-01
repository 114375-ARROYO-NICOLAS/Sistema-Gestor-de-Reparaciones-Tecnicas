package com.sigret.dtos.servicio;

import com.sigret.enums.EstadoServicio;
import com.sigret.enums.TipoIngreso;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioUpdateDto {

    private TipoIngreso tipoIngreso;

    private String firmaIngreso; // Base64 de la firma

    private String firmaConformidad; // Base64 de la firma

    // Campos de garantía
    private Boolean esGarantia;

    private Long servicioGarantiaId;

    private Boolean garantiaDentroPlazo;

    private Boolean garantiaCumpleCondiciones;

    private String observacionesGarantia;

    private Long tecnicoEvaluacionId;

    private String observacionesEvaluacionGarantia;

    // Campos existentes
    private Boolean abonaVisita;

    private BigDecimal montoVisita;

    private BigDecimal montoPagado;

    private EstadoServicio estado;

    private LocalDate fechaDevolucionPrevista;

    private LocalDate fechaDevolucionReal;
}
