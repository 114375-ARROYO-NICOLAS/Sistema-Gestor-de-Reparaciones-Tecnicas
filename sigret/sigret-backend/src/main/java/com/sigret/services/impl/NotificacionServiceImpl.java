package com.sigret.services.impl;

import com.sigret.dtos.notificacion.NotificacionDto;
import com.sigret.entities.Notificacion;
import com.sigret.entities.Usuario;
import com.sigret.enums.TipoReferencia;
import com.sigret.repositories.NotificacionRepository;
import com.sigret.repositories.UsuarioRepository;
import com.sigret.services.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificacionServiceImpl implements NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void crearNotificacionParaTodos(String mensaje, String tipo, Long referenciaId,
                                           TipoReferencia tipoReferencia, String icono, String severidad) {
        List<Usuario> usuarios = usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .toList();

        LocalDateTime ahora = LocalDateTime.now();

        List<Notificacion> notificaciones = usuarios.stream()
                .map(usuario -> {
                    Notificacion n = new Notificacion();
                    n.setMensaje(mensaje);
                    n.setTipo(tipo);
                    n.setLeida(false);
                    n.setFechaCreacion(ahora);
                    n.setUsuario(usuario);
                    n.setReferenciaId(referenciaId);
                    n.setTipoReferencia(tipoReferencia);
                    n.setIcono(icono);
                    n.setSeveridad(severidad);
                    return n;
                })
                .toList();

        notificacionRepository.saveAll(notificaciones);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDto> obtenerRecientes(Long usuarioId) {
        return notificacionRepository.findTop20ByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Override
    public void marcarComoLeida(Long notificacionId, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        if (!notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tiene permiso para modificar esta notificación");
        }

        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }

    @Override
    public void marcarTodasComoLeidas(Long usuarioId) {
        notificacionRepository.marcarTodasComoLeidasPorUsuario(usuarioId);
    }

    private NotificacionDto toDto(Notificacion n) {
        return new NotificacionDto(
                n.getId(),
                n.getMensaje(),
                n.getTipo(),
                n.getLeida(),
                n.getFechaCreacion(),
                n.getReferenciaId(),
                n.getTipoReferencia(),
                n.getIcono(),
                n.getSeveridad()
        );
    }
}
