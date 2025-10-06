package com.sigret.services;

import com.sigret.dtos.direccion.DireccionCreateDto;
import com.sigret.dtos.direccion.DireccionListDto;
import com.sigret.dtos.direccion.DireccionResponseDto;
import com.sigret.dtos.direccion.DireccionUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DireccionService {
    
    /**
     * Crear una nueva dirección
     */
    DireccionResponseDto crearDireccion(DireccionCreateDto direccionCreateDto);
    
    /**
     * Obtener una dirección por ID
     */
    DireccionResponseDto obtenerDireccionPorId(Long id);
    
    /**
     * Obtener todas las direcciones de una persona
     */
    List<DireccionListDto> obtenerDireccionesPorPersona(Long personaId);
    
    /**
     * Obtener la dirección principal de una persona
     */
    DireccionResponseDto obtenerDireccionPrincipalPorPersona(Long personaId);
    
    /**
     * Obtener todas las direcciones con paginación
     */
    Page<DireccionListDto> obtenerDirecciones(Pageable pageable);
    
    /**
     * Buscar direcciones por ciudad
     */
    List<DireccionListDto> buscarPorCiudad(String ciudad);
    
    /**
     * Buscar direcciones por provincia
     */
    List<DireccionListDto> buscarPorProvincia(String provincia);
    
    /**
     * Actualizar una dirección
     */
    DireccionResponseDto actualizarDireccion(Long id, DireccionUpdateDto direccionUpdateDto);
    
    /**
     * Eliminar una dirección
     */
    void eliminarDireccion(Long id);
    
    /**
     * Marcar una dirección como principal
     */
    DireccionResponseDto marcarComoPrincipal(Long id);
}

