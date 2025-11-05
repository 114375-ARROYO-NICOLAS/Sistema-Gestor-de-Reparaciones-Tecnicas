package com.sigret.dtos.servicio;

import com.sigret.enums.EstadoServicio;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioEventDto {

    private String tipo; // "CREADO", "ACTUALIZADO", "ESTADO_CAMBIADO"
    private Long servicioId;
    private String numeroServicio;
    private EstadoServicio estadoAnterior;
    private EstadoServicio estadoNuevo;
    private LocalDateTime timestamp;
    private ServicioListDto servicio;

    // Constructor para evento de creación
    public ServicioEventDto(String tipo, ServicioListDto servicio) {
        this.tipo = tipo;
        this.servicio = servicio;
        this.servicioId = servicio.getId();
        this.numeroServicio = servicio.getNumeroServicio();
        this.estadoNuevo = servicio.getEstado();
        this.timestamp = LocalDateTime.now();
    }

    // Constructor para evento de cambio de estado
    public ServicioEventDto(String tipo, ServicioListDto servicio, EstadoServicio estadoAnterior) {
        this.tipo = tipo;
        this.servicio = servicio;
        this.servicioId = servicio.getId();
        this.numeroServicio = servicio.getNumeroServicio();
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = servicio.getEstado();
        this.timestamp = LocalDateTime.now();
    }
}
