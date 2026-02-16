package com.sigret.services;

import com.sigret.dtos.notificacion.NotificacionDto;
import com.sigret.enums.TipoReferencia;

import java.util.List;

public interface NotificacionService {

    void crearNotificacionParaTodos(String mensaje, String tipo, Long referenciaId,
                                    TipoReferencia tipoReferencia, String icono, String severidad);

    List<NotificacionDto> obtenerRecientes(Long usuarioId);

    Long contarNoLeidas(Long usuarioId);

    void marcarComoLeida(Long notificacionId, Long usuarioId);

    void marcarTodasComoLeidas(Long usuarioId);
}
