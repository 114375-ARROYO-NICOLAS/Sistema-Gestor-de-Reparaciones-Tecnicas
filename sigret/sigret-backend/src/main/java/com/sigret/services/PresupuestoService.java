package com.sigret.services;

import com.sigret.dtos.presupuesto.PresupuestoCreateDto;
import com.sigret.dtos.presupuesto.PresupuestoListDto;
import com.sigret.dtos.presupuesto.PresupuestoResponseDto;
import com.sigret.dtos.presupuesto.PresupuestoUpdateDto;
import com.sigret.entities.Presupuesto;
import com.sigret.enums.EstadoPresupuesto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PresupuestoService {
    
    /**
     * Crear un nuevo presupuesto
     */
    PresupuestoResponseDto crearPresupuesto(PresupuestoCreateDto presupuestoCreateDto);
    
    /**
     * Obtener un presupuesto por ID
     */
    PresupuestoResponseDto obtenerPresupuestoPorId(Long id);
    
    /**
     * Obtener todos los presupuestos con paginación
     */
    Page<PresupuestoListDto> obtenerPresupuestos(Pageable pageable);
    
    /**
     * Obtener presupuestos por servicio
     */
    List<PresupuestoListDto> obtenerPresupuestosPorServicio(Long servicioId);
    
    /**
     * Obtener presupuestos por estado
     */
    List<PresupuestoListDto> obtenerPresupuestosPorEstado(EstadoPresupuesto estado);
    
    /**
     * Obtener presupuestos por cliente
     */
    List<PresupuestoListDto> obtenerPresupuestosPorCliente(Long clienteId);
    
    /**
     * Obtener presupuestos por rango de fechas
     */
    List<PresupuestoListDto> obtenerPresupuestosPorFechas(LocalDate fechaInicio, LocalDate fechaFin);
    
    /**
     * Actualizar un presupuesto
     */
    PresupuestoResponseDto actualizarPresupuesto(Long id, PresupuestoUpdateDto presupuestoUpdateDto);
    
    /**
     * Cambiar estado de un presupuesto
     */
    PresupuestoResponseDto cambiarEstadoPresupuesto(Long id, EstadoPresupuesto nuevoEstado);
    
    /**
     * Aprobar presupuesto
     */
    PresupuestoResponseDto aprobarPresupuesto(Long id);
    
    /**
     * Rechazar presupuesto
     */
    PresupuestoResponseDto rechazarPresupuesto(Long id);
    
    /**
     * Eliminar un presupuesto
     */
    void eliminarPresupuesto(Long id);
    
    /**
     * Generar número de presupuesto automático
     */
    String generarNumeroPresupuesto();
}
