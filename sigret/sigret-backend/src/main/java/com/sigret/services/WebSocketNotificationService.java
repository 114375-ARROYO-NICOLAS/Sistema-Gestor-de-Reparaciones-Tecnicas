package com.sigret.services;

import com.sigret.dtos.servicio.ServicioEventDto;
import com.sigret.dtos.servicio.ServicioListDto;
import com.sigret.dtos.presupuesto.PresupuestoEventDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoEventDto;
import com.sigret.enums.EstadoServicio;
import com.sigret.enums.TipoReferencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificacionService notificacionService;

    /**
     * Notifica la creación de un nuevo servicio
     */
    public void notificarServicioCreado(ServicioListDto servicio) {
        ServicioEventDto evento = new ServicioEventDto("CREADO", servicio);
        messagingTemplate.convertAndSend("/topic/servicios", evento);

        persistirYNotificar(
                "Nuevo servicio creado: " + servicio.getNumeroServicio(),
                "CREADO", servicio.getId(), TipoReferencia.SERVICIO, "pi pi-plus-circle", "info"
        );
    }

    /**
     * Notifica la actualización de un servicio
     */
    public void notificarServicioActualizado(ServicioListDto servicio) {
        ServicioEventDto evento = new ServicioEventDto("ACTUALIZADO", servicio);
        messagingTemplate.convertAndSend("/topic/servicios", evento);

        persistirYNotificar(
                "Servicio actualizado: " + servicio.getNumeroServicio(),
                "ACTUALIZADO", servicio.getId(), TipoReferencia.SERVICIO, "pi pi-pencil", "info"
        );
    }

    /**
     * Notifica el cambio de estado de un servicio
     */
    public void notificarCambioEstado(ServicioListDto servicio, EstadoServicio estadoAnterior) {
        ServicioEventDto evento = new ServicioEventDto("ESTADO_CAMBIADO", servicio, estadoAnterior);
        messagingTemplate.convertAndSend("/topic/servicios", evento);

        String mensaje = String.format("Servicio %s cambió de %s a %s",
                servicio.getNumeroServicio(),
                formatearEstado(estadoAnterior.name()),
                formatearEstado(servicio.getEstado().name()));

        persistirYNotificar(
                mensaje, "ESTADO_CAMBIADO", servicio.getId(), TipoReferencia.SERVICIO, "pi pi-sync", "success"
        );
    }

    /**
     * Notifica la eliminación de un servicio
     */
    public void notificarServicioEliminado(Long servicioId) {
        ServicioEventDto evento = new ServicioEventDto();
        evento.setTipo("ELIMINADO");
        evento.setServicioId(servicioId);
        messagingTemplate.convertAndSend("/topic/servicios", evento);

        persistirYNotificar(
                "Servicio eliminado (ID: " + servicioId + ")",
                "ELIMINADO", servicioId, TipoReferencia.SERVICIO, "pi pi-trash", "warn"
        );
    }

    /**
     * Notifica eventos de presupuesto
     */
    public void notificarPresupuesto(PresupuestoEventDto evento) {
        messagingTemplate.convertAndSend("/topic/presupuestos", evento);

        String accion = switch (evento.getTipoEvento()) {
            case "CREADO" -> "creado";
            case "ACTUALIZADO" -> "actualizado";
            case "CAMBIO_ESTADO" -> String.format("cambió a %s", formatearEstado(
                    evento.getEstadoNuevo() != null ? evento.getEstadoNuevo().name() : ""));
            case "ELIMINADO" -> "eliminado";
            default -> evento.getTipoEvento().toLowerCase();
        };

        String severidad = "ELIMINADO".equals(evento.getTipoEvento()) ? "warn" : "info";
        String icono = "ELIMINADO".equals(evento.getTipoEvento()) ? "pi pi-trash" : "pi pi-dollar";

        persistirYNotificar(
                String.format("Presupuesto %s del servicio %s", accion, evento.getNumeroServicio()),
                evento.getTipoEvento(), evento.getPresupuestoId(), TipoReferencia.PRESUPUESTO, icono, severidad
        );
    }

    /**
     * Notifica eventos de orden de trabajo
     */
    public void notificarOrdenTrabajo(OrdenTrabajoEventDto evento) {
        messagingTemplate.convertAndSend("/topic/ordenes-trabajo", evento);

        String accion = switch (evento.getTipoEvento()) {
            case "CREADO" -> "creada";
            case "ACTUALIZADO" -> "actualizada";
            case "CAMBIO_ESTADO" -> String.format("cambió a %s", formatearEstado(
                    evento.getEstadoNuevo() != null ? evento.getEstadoNuevo().name() : ""));
            case "ELIMINADO" -> "eliminada";
            default -> evento.getTipoEvento().toLowerCase();
        };

        String severidad = "ELIMINADO".equals(evento.getTipoEvento()) ? "warn" : "info";
        String icono = "ELIMINADO".equals(evento.getTipoEvento()) ? "pi pi-trash" : "pi pi-wrench";

        persistirYNotificar(
                String.format("Orden de trabajo %s del servicio %s", accion, evento.getNumeroServicio()),
                evento.getTipoEvento(), evento.getOrdenTrabajoId(), TipoReferencia.ORDEN_TRABAJO, icono, severidad
        );
    }

    private void persistirYNotificar(String mensaje, String tipo, Long referenciaId,
                                     TipoReferencia tipoReferencia, String icono, String severidad) {
        notificacionService.crearNotificacionParaTodos(mensaje, tipo, referenciaId, tipoReferencia, icono, severidad);
        // Enviar señal de refresh al frontend
        messagingTemplate.convertAndSend("/topic/notificaciones", Map.of("tipo", "NUEVA_NOTIFICACION"));
    }

    private String formatearEstado(String estado) {
        if (estado == null || estado.isEmpty()) return "";
        return estado.replace("_", " ");
    }
}
