package com.sigret.dtos.notificacion;

import com.sigret.enums.TipoReferencia;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDto {
    private Long id;
    private String mensaje;
    private String tipo;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private Long referenciaId;
    private TipoReferencia tipoReferencia;
    private String icono;
    private String severidad;
}
