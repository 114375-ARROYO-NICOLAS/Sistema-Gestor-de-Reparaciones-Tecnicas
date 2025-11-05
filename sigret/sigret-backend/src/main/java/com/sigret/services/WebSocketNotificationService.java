package com.sigret.services;

import com.sigret.dtos.servicio.ServicioEventDto;
import com.sigret.dtos.servicio.ServicioListDto;
import com.sigret.dtos.presupuesto.PresupuestoEventDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoEventDto;
import com.sigret.enums.EstadoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Notifica la creación de un nuevo servicio
     */
    public void notificarServicioCreado(ServicioListDto servicio) {
        ServicioEventDto evento = new ServicioEventDto("CREADO", servicio);
        messagingTemplate.convertAndSend("/topic/servicios", evento);
    }

    /**
     * Notifica la actualización de un servicio
     */
    public void notificarServicioActualizado(ServicioListDto servicio) {
        ServicioEventDto evento = new ServicioEventDto("ACTUALIZADO", servicio);
        messagingTemplate.convertAndSend("/topic/servicios", evento);
    }

    /**
     * Notifica el cambio de estado de un servicio
     */
    public void notificarCambioEstado(ServicioListDto servicio, EstadoServicio estadoAnterior) {
        ServicioEventDto evento = new ServicioEventDto("ESTADO_CAMBIADO", servicio, estadoAnterior);
        messagingTemplate.convertAndSend("/topic/servicios", evento);
    }

    /**
     * Notifica la eliminación de un servicio
     */
    public void notificarServicioEliminado(Long servicioId) {
        ServicioEventDto evento = new ServicioEventDto();
        evento.setTipo("ELIMINADO");
        evento.setServicioId(servicioId);
        messagingTemplate.convertAndSend("/topic/servicios", evento);
    }

    /**
     * Notifica eventos de presupuesto
     */
    public void notificarPresupuesto(PresupuestoEventDto evento) {
        messagingTemplate.convertAndSend("/topic/presupuestos", evento);
    }

    /**
     * Notifica eventos de orden de trabajo
     */
    public void notificarOrdenTrabajo(OrdenTrabajoEventDto evento) {
        messagingTemplate.convertAndSend("/topic/ordenes-trabajo", evento);
    }
}
