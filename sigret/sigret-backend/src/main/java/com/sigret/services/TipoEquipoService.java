package com.sigret.services;

import com.sigret.dtos.tipoEquipo.TipoEquipoCreateDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoListDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoResponseDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoUpdateDto;

import java.util.List;

public interface TipoEquipoService {

    /**
     * Crear un nuevo tipo de equipo
     */
    TipoEquipoResponseDto crearTipoEquipo(TipoEquipoCreateDto tipoEquipoCreateDto);

    /**
     * Obtener un tipo de equipo por ID
     */
    TipoEquipoResponseDto obtenerTipoEquipoPorId(Long id);

    /**
     * Obtener todos los tipos de equipo
     */
    List<TipoEquipoListDto> obtenerTodosLosTiposEquipo();

    /**
     * Actualizar un tipo de equipo
     */
    TipoEquipoResponseDto actualizarTipoEquipo(Long id, TipoEquipoUpdateDto tipoEquipoUpdateDto);

    /**
     * Eliminar un tipo de equipo
     */
    void eliminarTipoEquipo(Long id);
}
