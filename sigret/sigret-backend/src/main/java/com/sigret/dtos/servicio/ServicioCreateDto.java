package com.sigret.dtos.servicio;

import com.sigret.dtos.detalleservicio.DetalleServicioDto;
import com.sigret.enums.TipoIngreso;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioCreateDto {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El equipo es obligatorio")
    private Long equipoId;

    @NotNull(message = "El empleado de recepción es obligatorio")
    private Long empleadoRecepcionId;

    @NotNull(message = "El tipo de ingreso es obligatorio")
    private TipoIngreso tipoIngreso;

    private String firmaIngreso; // Base64 de la firma

    private String firmaConformidad; // Base64 de la firma

    // Campos de garantía (para futuros sprints)
    private Boolean esGarantia = false;

    private Long servicioGarantiaId; // Referencia al servicio original para garantías

    private Boolean garantiaDentroPlazo;

    private Boolean garantiaCumpleCondiciones;

    private String observacionesGarantia;

    private Long tecnicoEvaluacionId;

    private String observacionesEvaluacionGarantia;

    // Campos existentes
    private Boolean abonaVisita = false;

    private BigDecimal montoVisita = BigDecimal.ZERO;

    private BigDecimal montoPagado;

    // Detalles del servicio (componentes del equipo)
    @Valid
    private List<DetalleServicioDto> detalles = new ArrayList<>();
}
