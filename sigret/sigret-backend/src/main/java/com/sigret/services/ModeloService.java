package com.sigret.services;

import com.sigret.dtos.modelo.ModeloCreateDto;
import com.sigret.dtos.modelo.ModeloListDto;
import com.sigret.dtos.modelo.ModeloResponseDto;
import com.sigret.dtos.modelo.ModeloUpdateDto;
import com.sigret.entities.Modelo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ModeloService {
    
    /**
     * Crear un nuevo modelo
     */
    ModeloResponseDto crearModelo(ModeloCreateDto modeloCreateDto);
    
    /**
     * Obtener un modelo por ID
     */
    ModeloResponseDto obtenerModeloPorId(Long id);
    
    /**
     * Obtener todos los modelos con paginación
     */
    Page<ModeloListDto> obtenerModelos(Pageable pageable);
    
    /**
     * Obtener todos los modelos
     */
    List<ModeloListDto> obtenerTodosLosModelos();
    
    /**
     * Obtener modelos por marca
     */
    List<ModeloListDto> obtenerModelosPorMarca(Long marcaId);
    
    /**
     * Buscar modelos por descripción
     */
    List<ModeloListDto> buscarModelos(String termino);
    
    /**
     * Actualizar un modelo
     */
    ModeloResponseDto actualizarModelo(Long id, ModeloUpdateDto modeloUpdateDto);
    
    /**
     * Eliminar un modelo
     */
    void eliminarModelo(Long id);
    
    /**
     * Verificar si un modelo ya existe para una marca
     */
    boolean existeModeloConDescripcionYMarca(String descripcion, Long marcaId);
}
