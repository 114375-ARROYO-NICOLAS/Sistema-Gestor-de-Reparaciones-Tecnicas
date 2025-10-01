package com.sigret.services;

import com.sigret.dtos.servicio.ServicioCreateDto;
import com.sigret.dtos.servicio.ServicioListDto;
import com.sigret.dtos.servicio.ServicioResponseDto;
import com.sigret.dtos.servicio.ServicioUpdateDto;
import com.sigret.entities.Servicio;
import com.sigret.enums.EstadoServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ServicioService {
    
    /**
     * Crear un nuevo servicio
     */
    ServicioResponseDto crearServicio(ServicioCreateDto servicioCreateDto);
    
    /**
     * Obtener un servicio por ID
     */
    ServicioResponseDto obtenerServicioPorId(Long id);
    
    /**
     * Obtener todos los servicios con paginación
     */
    Page<ServicioListDto> obtenerServicios(Pageable pageable);
    
    /**
     * Obtener servicios por estado
     */
    List<ServicioListDto> obtenerServiciosPorEstado(EstadoServicio estado);
    
    /**
     * Obtener servicios por cliente
     */
    List<ServicioListDto> obtenerServiciosPorCliente(Long clienteId);
    
    /**
     * Obtener servicios por rango de fechas
     */
    List<ServicioListDto> obtenerServiciosPorFechas(LocalDate fechaInicio, LocalDate fechaFin);
    
    /**
     * Buscar servicios por número de servicio
     */
    ServicioResponseDto obtenerServicioPorNumero(String numeroServicio);
    
    /**
     * Actualizar un servicio
     */
    ServicioResponseDto actualizarServicio(Long id, ServicioUpdateDto servicioUpdateDto);
    
    /**
     * Cambiar estado de un servicio
     */
    ServicioResponseDto cambiarEstadoServicio(Long id, EstadoServicio nuevoEstado);
    
    /**
     * Eliminar un servicio
     */
    void eliminarServicio(Long id);
    
    /**
     * Generar número de servicio automático
     */
    String generarNumeroServicio();
    
    /**
     * Obtener servicios de garantía
     */
    List<ServicioListDto> obtenerServiciosGarantia();
    
    /**
     * Crear servicio de garantía
     */
    ServicioResponseDto crearServicioGarantia(Long servicioOriginalId, ServicioCreateDto servicioGarantiaDto);
}
