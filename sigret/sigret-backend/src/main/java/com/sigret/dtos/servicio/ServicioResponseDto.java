package com.sigret.dtos.servicio;

import com.sigret.dtos.detalleservicio.DetalleServicioDto;
import com.sigret.enums.EstadoServicio;
import com.sigret.enums.TipoIngreso;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioResponseDto {

    private Long id;
    private String numeroServicio;
    private Long clienteId;
    private String clienteNombre;
    private String clienteDocumento;
    private Long equipoId;
    private String equipoDescripcion;
    private String equipoNumeroSerie;
    private Long empleadoRecepcionId;
    private String empleadoRecepcionNombre;
    private TipoIngreso tipoIngreso;
    private String firmaIngreso;
    private String firmaConformidad;

    // Campos de garantía
    private Boolean esGarantia;
    private Long servicioGarantiaId;
    private String servicioGarantiaNumero;
    private Boolean garantiaDentroPlazo;
    private Boolean garantiaCumpleCondiciones;
    private String observacionesGarantia;
    private Long tecnicoEvaluacionId;
    private String tecnicoEvaluacionNombre;
    private LocalDateTime fechaEvaluacionGarantia;
    private String observacionesEvaluacionGarantia;

    // Campos existentes
    private Boolean abonaVisita;
    private BigDecimal montoVisita;
    private BigDecimal montoPagado;
    private EstadoServicio estado;
    private LocalDateTime fechaCreacion;
    private LocalDate fechaRecepcion;
    private LocalDate fechaDevolucionPrevista;
    private LocalDate fechaDevolucionReal;
    private String descripcionCompleta;

    // Detalles del servicio
    private List<DetalleServicioDto> detalles = new ArrayList<>();
}
