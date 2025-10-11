package com.sigret.services;

import com.sigret.dtos.ordenTrabajo.OrdenTrabajoCreateDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoListDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoResponseDto;
import com.sigret.dtos.ordenTrabajo.OrdenTrabajoUpdateDto;

import com.sigret.enums.EstadoOrdenTrabajo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OrdenTrabajoService {
    
    /**
     * Crear una nueva orden de trabajo
     */
    OrdenTrabajoResponseDto crearOrdenTrabajo(OrdenTrabajoCreateDto ordenTrabajoCreateDto);
    
    /**
     * Obtener una orden de trabajo por ID
     */
    OrdenTrabajoResponseDto obtenerOrdenTrabajoPorId(Long id);
    
    /**
     * Obtener todas las órdenes de trabajo con paginación
     */
    Page<OrdenTrabajoListDto> obtenerOrdenesTrabajo(Pageable pageable);
    
    /**
     * Obtener órdenes de trabajo por estado
     */
    List<OrdenTrabajoListDto> obtenerOrdenesTrabajoPorEstado(EstadoOrdenTrabajo estado);
    
    /**
     * Obtener órdenes de trabajo por empleado
     */
    List<OrdenTrabajoListDto> obtenerOrdenesTrabajoPorEmpleado(Long empleadoId);
    
    /**
     * Obtener órdenes de trabajo por servicio
     */
    List<OrdenTrabajoListDto> obtenerOrdenesTrabajoPorServicio(Long servicioId);
    
    /**
     * Obtener órdenes de trabajo por rango de fechas
     */
    List<OrdenTrabajoListDto> obtenerOrdenesTrabajoPorFechas(LocalDate fechaInicio, LocalDate fechaFin);
    
    /**
     * Actualizar una orden de trabajo
     */
    OrdenTrabajoResponseDto actualizarOrdenTrabajo(Long id, OrdenTrabajoUpdateDto ordenTrabajoUpdateDto);
    
    /**
     * Cambiar estado de una orden de trabajo
     */
    OrdenTrabajoResponseDto cambiarEstadoOrdenTrabajo(Long id, EstadoOrdenTrabajo nuevoEstado);
    
    /**
     * Iniciar orden de trabajo
     */
    OrdenTrabajoResponseDto iniciarOrdenTrabajo(Long id);
    
    /**
     * Finalizar orden de trabajo
     */
    OrdenTrabajoResponseDto finalizarOrdenTrabajo(Long id);
    
    /**
     * Eliminar una orden de trabajo
     */
    void eliminarOrdenTrabajo(Long id);
    
    /**
     * Obtener órdenes de trabajo sin costo (garantías)
     */
    List<OrdenTrabajoListDto> obtenerOrdenesTrabajoSinCosto();
}
