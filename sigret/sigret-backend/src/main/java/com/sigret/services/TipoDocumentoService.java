package com.sigret.services;

import com.sigret.dtos.tipoDocumento.TipoDocumentoCreateDto;
import com.sigret.dtos.tipoDocumento.TipoDocumentoListDto;
import com.sigret.dtos.tipoDocumento.TipoDocumentoResponseDto;
import com.sigret.dtos.tipoDocumento.TipoDocumentoUpdateDto;
import com.sigret.entities.TipoDocumento;
import com.sigret.exception.TipoAlreadyExistsException;
import com.sigret.exception.TipoDocumentoNotFoundException;
import com.sigret.repositories.TipoDocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TipoDocumentoService {

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    /**
     * Crear un nuevo tipo de documento
     */
    public TipoDocumentoResponseDto crearTipoDocumento(TipoDocumentoCreateDto tipoDocumentoCreateDto) {
        // Validar que no exista otro tipo de documento con la misma descripción
        if (tipoDocumentoRepository.existsByDescripcionIgnoreCase(tipoDocumentoCreateDto.getDescripcion())) {
            throw new TipoAlreadyExistsException("Ya existe un tipo de documento con la descripción: " + tipoDocumentoCreateDto.getDescripcion());
        }

        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setDescripcion(tipoDocumentoCreateDto.getDescripcion());

        TipoDocumento tipoDocumentoGuardado = tipoDocumentoRepository.save(tipoDocumento);
        return convertirATipoDocumentoResponseDto(tipoDocumentoGuardado);
    }

    /**
     * Obtener un tipo de documento por ID
     */
    @Transactional(readOnly = true)
    public TipoDocumentoResponseDto obtenerTipoDocumentoPorId(Long id) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new TipoDocumentoNotFoundException("Tipo de documento no encontrado con ID: " + id));

        return convertirATipoDocumentoResponseDto(tipoDocumento);
    }

    /**
     * Obtener todos los tipos de documento
     */
    @Transactional(readOnly = true)
    public List<TipoDocumentoListDto> obtenerTodosTiposDocumento() {
        List<TipoDocumento> tiposDocumento = tipoDocumentoRepository.findAll();
        return tiposDocumento.stream()
                .map(this::convertirATipoDocumentoListDto)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar un tipo de documento
     */
    public TipoDocumentoResponseDto actualizarTipoDocumento(Long id, TipoDocumentoUpdateDto tipoDocumentoUpdateDto) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new TipoDocumentoNotFoundException("Tipo de documento no encontrado con ID: " + id));

        // Validar que no exista otro tipo de documento con la misma descripción
        if (!tipoDocumento.getDescripcion().equalsIgnoreCase(tipoDocumentoUpdateDto.getDescripcion()) &&
            tipoDocumentoRepository.existsByDescripcionIgnoreCase(tipoDocumentoUpdateDto.getDescripcion())) {
            throw new TipoAlreadyExistsException("Ya existe un tipo de documento con la descripción: " + tipoDocumentoUpdateDto.getDescripcion());
        }

        tipoDocumento.setDescripcion(tipoDocumentoUpdateDto.getDescripcion());

        TipoDocumento tipoDocumentoActualizado = tipoDocumentoRepository.save(tipoDocumento);
        return convertirATipoDocumentoResponseDto(tipoDocumentoActualizado);
    }

    /**
     * Eliminar un tipo de documento
     */
    public void eliminarTipoDocumento(Long id) {
        if (!tipoDocumentoRepository.existsById(id)) {
            throw new TipoDocumentoNotFoundException("Tipo de documento no encontrado con ID: " + id);
        }
        tipoDocumentoRepository.deleteById(id);
    }

    /**
     * Convertir TipoDocumento a TipoDocumentoResponseDto
     */
    private TipoDocumentoResponseDto convertirATipoDocumentoResponseDto(TipoDocumento tipoDocumento) {
        return new TipoDocumentoResponseDto(
                tipoDocumento.getId(),
                tipoDocumento.getDescripcion()
        );
    }

    /**
     * Convertir TipoDocumento a TipoDocumentoListDto
     */
    private TipoDocumentoListDto convertirATipoDocumentoListDto(TipoDocumento tipoDocumento) {
        return new TipoDocumentoListDto(
                tipoDocumento.getId(),
                tipoDocumento.getDescripcion()
        );
    }
}

