package com.sigret.services;

import com.sigret.dtos.tipoEmpleado.TipoEmpleadoCreateDto;
import com.sigret.dtos.tipoEmpleado.TipoEmpleadoListDto;
import com.sigret.dtos.tipoEmpleado.TipoEmpleadoResponseDto;
import com.sigret.dtos.tipoEmpleado.TipoEmpleadoUpdateDto;
import com.sigret.entities.TipoEmpleado;
import com.sigret.exception.TipoAlreadyExistsException;
import com.sigret.exception.TipoEmpleadoNotFoundException;
import com.sigret.repositories.TipoEmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TipoEmpleadoService {

    @Autowired
    private TipoEmpleadoRepository tipoEmpleadoRepository;

    /**
     * Crear un nuevo tipo de empleado
     */
    public TipoEmpleadoResponseDto crearTipoEmpleado(TipoEmpleadoCreateDto tipoEmpleadoCreateDto) {
        // Validar que no exista otro tipo de empleado con la misma descripción
        if (tipoEmpleadoRepository.existsByDescripcionIgnoreCase(tipoEmpleadoCreateDto.getDescripcion())) {
            throw new TipoAlreadyExistsException("Ya existe un tipo de empleado con la descripción: " + tipoEmpleadoCreateDto.getDescripcion());
        }

        TipoEmpleado tipoEmpleado = new TipoEmpleado();
        tipoEmpleado.setDescripcion(tipoEmpleadoCreateDto.getDescripcion());

        TipoEmpleado tipoEmpleadoGuardado = tipoEmpleadoRepository.save(tipoEmpleado);
        return convertirATipoEmpleadoResponseDto(tipoEmpleadoGuardado);
    }

    /**
     * Obtener un tipo de empleado por ID
     */
    @Transactional(readOnly = true)
    public TipoEmpleadoResponseDto obtenerTipoEmpleadoPorId(Long id) {
        TipoEmpleado tipoEmpleado = tipoEmpleadoRepository.findById(id)
                .orElseThrow(() -> new TipoEmpleadoNotFoundException("Tipo de empleado no encontrado con ID: " + id));

        return convertirATipoEmpleadoResponseDto(tipoEmpleado);
    }

    /**
     * Obtener todos los tipos de empleado
     */
    @Transactional(readOnly = true)
    public List<TipoEmpleadoListDto> obtenerTodosTiposEmpleado() {
        List<TipoEmpleado> tiposEmpleado = tipoEmpleadoRepository.findAll();
        return tiposEmpleado.stream()
                .map(this::convertirATipoEmpleadoListDto)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar un tipo de empleado
     */
    public TipoEmpleadoResponseDto actualizarTipoEmpleado(Long id, TipoEmpleadoUpdateDto tipoEmpleadoUpdateDto) {
        TipoEmpleado tipoEmpleado = tipoEmpleadoRepository.findById(id)
                .orElseThrow(() -> new TipoEmpleadoNotFoundException("Tipo de empleado no encontrado con ID: " + id));

        // Validar que no exista otro tipo de empleado con la misma descripción
        if (!tipoEmpleado.getDescripcion().equalsIgnoreCase(tipoEmpleadoUpdateDto.getDescripcion()) &&
            tipoEmpleadoRepository.existsByDescripcionIgnoreCase(tipoEmpleadoUpdateDto.getDescripcion())) {
            throw new TipoAlreadyExistsException("Ya existe un tipo de empleado con la descripción: " + tipoEmpleadoUpdateDto.getDescripcion());
        }

        tipoEmpleado.setDescripcion(tipoEmpleadoUpdateDto.getDescripcion());

        TipoEmpleado tipoEmpleadoActualizado = tipoEmpleadoRepository.save(tipoEmpleado);
        return convertirATipoEmpleadoResponseDto(tipoEmpleadoActualizado);
    }

    /**
     * Eliminar un tipo de empleado
     */
    public void eliminarTipoEmpleado(Long id) {
        if (!tipoEmpleadoRepository.existsById(id)) {
            throw new TipoEmpleadoNotFoundException("Tipo de empleado no encontrado con ID: " + id);
        }
        tipoEmpleadoRepository.deleteById(id);
    }

    /**
     * Convertir TipoEmpleado a TipoEmpleadoResponseDto
     */
    private TipoEmpleadoResponseDto convertirATipoEmpleadoResponseDto(TipoEmpleado tipoEmpleado) {
        return new TipoEmpleadoResponseDto(
                tipoEmpleado.getId(),
                tipoEmpleado.getDescripcion()
        );
    }

    /**
     * Convertir TipoEmpleado a TipoEmpleadoListDto
     */
    private TipoEmpleadoListDto convertirATipoEmpleadoListDto(TipoEmpleado tipoEmpleado) {
        return new TipoEmpleadoListDto(
                tipoEmpleado.getId(),
                tipoEmpleado.getDescripcion()
        );
    }
}

