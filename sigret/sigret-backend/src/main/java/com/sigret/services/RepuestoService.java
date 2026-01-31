package com.sigret.services;

import com.sigret.dtos.repuesto.RepuestoCreateDto;
import com.sigret.dtos.repuesto.RepuestoListDto;
import com.sigret.dtos.repuesto.RepuestoResponseDto;
import com.sigret.dtos.repuesto.RepuestoUpdateDto;

import java.util.List;

public interface RepuestoService {

    /**
     * Crear un nuevo repuesto
     */
    RepuestoResponseDto crearRepuesto(RepuestoCreateDto repuestoCreateDto);

    /**
     * Obtener un repuesto por ID
     */
    RepuestoResponseDto obtenerRepuestoPorId(Long id);

    /**
     * Obtener todos los repuestos
     */
    List<RepuestoListDto> obtenerTodosLosRepuestos();

    /**
     * Obtener repuestos por tipo de equipo
     */
    List<RepuestoListDto> obtenerRepuestosPorTipoEquipo(Long tipoEquipoId);

    /**
     * Buscar repuestos por término
     */
    List<RepuestoListDto> buscarRepuestos(String termino);

    /**
     * Actualizar un repuesto
     */
    RepuestoResponseDto actualizarRepuesto(Long id, RepuestoUpdateDto repuestoUpdateDto);

    /**
     * Eliminar un repuesto
     */
    void eliminarRepuesto(Long id);

    /**
     * Verificar si un repuesto ya existe con la misma descripción y tipo de equipo
     */
    boolean existeRepuesto(String descripcion, Long tipoEquipoId);
}
