package com.sigret.services;

import com.sigret.dtos.tipoPersona.TipoPersonaCreateDto;
import com.sigret.dtos.tipoPersona.TipoPersonaListDto;
import com.sigret.dtos.tipoPersona.TipoPersonaResponseDto;
import com.sigret.dtos.tipoPersona.TipoPersonaUpdateDto;
import com.sigret.entities.TipoPersona;
import com.sigret.exception.TipoAlreadyExistsException;
import com.sigret.exception.TipoPersonaNotFoundException;
import com.sigret.repositories.TipoPersonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TipoPersonaService {

    @Autowired
    private TipoPersonaRepository tipoPersonaRepository;

    /**
     * Crear un nuevo tipo de persona
     */
    public TipoPersonaResponseDto crearTipoPersona(TipoPersonaCreateDto tipoPersonaCreateDto) {
        // Validar que no exista otro tipo de persona con la misma descripción
        if (tipoPersonaRepository.existsByDescripcionIgnoreCase(tipoPersonaCreateDto.getDescripcion())) {
            throw new TipoAlreadyExistsException("Ya existe un tipo de persona con la descripción: " + tipoPersonaCreateDto.getDescripcion());
        }

        TipoPersona tipoPersona = new TipoPersona();
        tipoPersona.setDescripcion(tipoPersonaCreateDto.getDescripcion());

        TipoPersona tipoPersonaGuardado = tipoPersonaRepository.save(tipoPersona);
        return convertirATipoPersonaResponseDto(tipoPersonaGuardado);
    }

    /**
     * Obtener un tipo de persona por ID
     */
    @Transactional(readOnly = true)
    public TipoPersonaResponseDto obtenerTipoPersonaPorId(Long id) {
        TipoPersona tipoPersona = tipoPersonaRepository.findById(id)
                .orElseThrow(() -> new TipoPersonaNotFoundException("Tipo de persona no encontrado con ID: " + id));

        return convertirATipoPersonaResponseDto(tipoPersona);
    }

    /**
     * Obtener todos los tipos de persona
     */
    @Transactional(readOnly = true)
    public List<TipoPersonaListDto> obtenerTodosTiposPersona() {
        List<TipoPersona> tiposPersona = tipoPersonaRepository.findAll();
        return tiposPersona.stream()
                .map(this::convertirATipoPersonaListDto)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar un tipo de persona
     */
    public TipoPersonaResponseDto actualizarTipoPersona(Long id, TipoPersonaUpdateDto tipoPersonaUpdateDto) {
        TipoPersona tipoPersona = tipoPersonaRepository.findById(id)
                .orElseThrow(() -> new TipoPersonaNotFoundException("Tipo de persona no encontrado con ID: " + id));

        // Validar que no exista otro tipo de persona con la misma descripción
        if (!tipoPersona.getDescripcion().equalsIgnoreCase(tipoPersonaUpdateDto.getDescripcion()) &&
            tipoPersonaRepository.existsByDescripcionIgnoreCase(tipoPersonaUpdateDto.getDescripcion())) {
            throw new TipoAlreadyExistsException("Ya existe un tipo de persona con la descripción: " + tipoPersonaUpdateDto.getDescripcion());
        }

        tipoPersona.setDescripcion(tipoPersonaUpdateDto.getDescripcion());

        TipoPersona tipoPersonaActualizado = tipoPersonaRepository.save(tipoPersona);
        return convertirATipoPersonaResponseDto(tipoPersonaActualizado);
    }

    /**
     * Eliminar un tipo de persona
     */
    public void eliminarTipoPersona(Long id) {
        if (!tipoPersonaRepository.existsById(id)) {
            throw new TipoPersonaNotFoundException("Tipo de persona no encontrado con ID: " + id);
        }
        tipoPersonaRepository.deleteById(id);
    }

    /**
     * Convertir TipoPersona a TipoPersonaResponseDto
     */
    private TipoPersonaResponseDto convertirATipoPersonaResponseDto(TipoPersona tipoPersona) {
        return new TipoPersonaResponseDto(
                tipoPersona.getId(),
                tipoPersona.getDescripcion()
        );
    }

    /**
     * Convertir TipoPersona a TipoPersonaListDto
     */
    private TipoPersonaListDto convertirATipoPersonaListDto(TipoPersona tipoPersona) {
        return new TipoPersonaListDto(
                tipoPersona.getId(),
                tipoPersona.getDescripcion()
        );
    }
}

