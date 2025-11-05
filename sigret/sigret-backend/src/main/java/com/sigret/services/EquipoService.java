package com.sigret.services;

import com.sigret.dtos.equipo.EquipoCreateDto;
import com.sigret.dtos.equipo.EquipoListDto;
import com.sigret.dtos.equipo.EquipoResponseDto;
import com.sigret.dtos.equipo.EquipoUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EquipoService {
    
    /**
     * Crear un nuevo equipo
     */
    EquipoResponseDto crearEquipo(EquipoCreateDto equipoCreateDto);
    
    /**
     * Obtener un equipo por ID
     */
    EquipoResponseDto obtenerEquipoPorId(Long id);
    
    /**
     * Obtener todos los equipos con paginación
     */
    Page<EquipoListDto> obtenerEquipos(Pageable pageable);
    
    /**
     * Obtener todos los equipos
     */
    List<EquipoListDto> obtenerTodosLosEquipos();
    
    /**
     * Buscar equipos por número de serie o descripción
     */
    List<EquipoListDto> buscarEquipos(String termino);
    
    /**
     * Obtener equipos por marca
     */
    List<EquipoListDto> obtenerEquiposPorMarca(Long marcaId);
    
    /**
     * Obtener equipos por tipo
     */
    List<EquipoListDto> obtenerEquiposPorTipo(Long tipoEquipoId);
    
    /**
     * Actualizar un equipo
     */
    EquipoResponseDto actualizarEquipo(Long id, EquipoUpdateDto equipoUpdateDto);
    
    /**
     * Eliminar un equipo
     */
    void eliminarEquipo(Long id);
    
    /**
     * Verificar si un número de serie ya existe
     */
    boolean existeEquipoConNumeroSerie(String numeroSerie);

    /**
     * Obtener equipos de un cliente específico
     */
    List<EquipoListDto> obtenerEquiposPorCliente(Long clienteId);

    /**
     * Asociar un equipo a un cliente
     */
    void asociarEquipoACliente(Long equipoId, Long clienteId);

    /**
     * Desasociar un equipo de un cliente
     */
    void desasociarEquipoDeCliente(Long equipoId, Long clienteId);
}
