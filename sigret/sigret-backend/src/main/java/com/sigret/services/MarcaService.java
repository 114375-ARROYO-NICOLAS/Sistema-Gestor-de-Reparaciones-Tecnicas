package com.sigret.services;

import com.sigret.dtos.marca.MarcaCreateDto;
import com.sigret.dtos.marca.MarcaListDto;
import com.sigret.dtos.marca.MarcaResponseDto;
import com.sigret.dtos.marca.MarcaUpdateDto;
import com.sigret.entities.Marca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MarcaService {
    
    /**
     * Crear una nueva marca
     */
    MarcaResponseDto crearMarca(MarcaCreateDto marcaCreateDto);
    
    /**
     * Obtener una marca por ID
     */
    MarcaResponseDto obtenerMarcaPorId(Long id);
    
    /**
     * Obtener todas las marcas con paginación
     */
    Page<MarcaListDto> obtenerMarcas(Pageable pageable);
    
    /**
     * Obtener todas las marcas
     */
    List<MarcaListDto> obtenerTodasLasMarcas();
    
    /**
     * Buscar marcas por descripción
     */
    List<MarcaListDto> buscarMarcas(String termino);
    
    /**
     * Actualizar una marca
     */
    MarcaResponseDto actualizarMarca(Long id, MarcaUpdateDto marcaUpdateDto);
    
    /**
     * Eliminar una marca
     */
    void eliminarMarca(Long id);
    
    /**
     * Verificar si una marca ya existe
     */
    boolean existeMarcaConDescripcion(String descripcion);
}
