package com.sigret.services.impl;

import com.sigret.dtos.repuesto.RepuestoCreateDto;
import com.sigret.dtos.repuesto.RepuestoListDto;
import com.sigret.dtos.repuesto.RepuestoResponseDto;
import com.sigret.dtos.repuesto.RepuestoUpdateDto;
import com.sigret.entities.Repuesto;
import com.sigret.entities.TipoEquipo;
import com.sigret.exception.RepuestoAlreadyExistsException;
import com.sigret.exception.RepuestoNotFoundException;
import com.sigret.exception.TipoEquipoNotFoundException;
import com.sigret.repositories.RepuestoRepository;
import com.sigret.repositories.TipoEquipoRepository;
import com.sigret.services.RepuestoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RepuestoServiceImpl implements RepuestoService {

    @Autowired
    private RepuestoRepository repuestoRepository;

    @Autowired
    private TipoEquipoRepository tipoEquipoRepository;

    @Override
    public RepuestoResponseDto crearRepuesto(RepuestoCreateDto repuestoCreateDto) {
        // Validar que el tipo de equipo existe
        TipoEquipo tipoEquipo = tipoEquipoRepository.findById(repuestoCreateDto.getTipoEquipoId())
                .orElseThrow(() -> new TipoEquipoNotFoundException(
                        "Tipo de equipo no encontrado con ID: " + repuestoCreateDto.getTipoEquipoId()));

        // Validar que el repuesto no existe
        if (existeRepuesto(repuestoCreateDto.getDescripcion(), repuestoCreateDto.getTipoEquipoId())) {
            throw new RepuestoAlreadyExistsException(
                    "Ya existe un repuesto con la misma descripción para este tipo de equipo");
        }

        // Crear el repuesto
        Repuesto repuesto = new Repuesto();
        repuesto.setDescripcion(repuestoCreateDto.getDescripcion());
        repuesto.setTipoEquipo(tipoEquipo);

        Repuesto repuestoGuardado = repuestoRepository.save(repuesto);

        return convertirARepuestoResponseDto(repuestoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public RepuestoResponseDto obtenerRepuestoPorId(Long id) {
        Repuesto repuesto = repuestoRepository.findById(id)
                .orElseThrow(() -> new RepuestoNotFoundException("Repuesto no encontrado con ID: " + id));

        return convertirARepuestoResponseDto(repuesto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepuestoListDto> obtenerTodosLosRepuestos() {
        List<Repuesto> repuestos = repuestoRepository.findAll();
        return repuestos.stream()
                .map(this::convertirARepuestoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepuestoListDto> obtenerRepuestosPorTipoEquipo(Long tipoEquipoId) {
        List<Repuesto> repuestos = repuestoRepository.findByTipoEquipoId(tipoEquipoId);
        return repuestos.stream()
                .map(this::convertirARepuestoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepuestoListDto> buscarRepuestos(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodosLosRepuestos();
        }
        List<Repuesto> repuestos = repuestoRepository.buscarPorTermino(termino);
        return repuestos.stream()
                .map(this::convertirARepuestoListDto)
                .collect(Collectors.toList());
    }

    @Override
    public RepuestoResponseDto actualizarRepuesto(Long id, RepuestoUpdateDto repuestoUpdateDto) {
        Repuesto repuesto = repuestoRepository.findById(id)
                .orElseThrow(() -> new RepuestoNotFoundException("Repuesto no encontrado con ID: " + id));

        // Actualizar tipo de equipo si se proporciona
        if (repuestoUpdateDto.getTipoEquipoId() != null) {
            TipoEquipo tipoEquipo = tipoEquipoRepository.findById(repuestoUpdateDto.getTipoEquipoId())
                    .orElseThrow(() -> new TipoEquipoNotFoundException(
                            "Tipo de equipo no encontrado con ID: " + repuestoUpdateDto.getTipoEquipoId()));
            repuesto.setTipoEquipo(tipoEquipo);
        }

        // Actualizar descripción si se proporciona
        if (repuestoUpdateDto.getDescripcion() != null && !repuestoUpdateDto.getDescripcion().trim().isEmpty()) {
            // Validar que no exista otro repuesto con la misma descripción y tipo de equipo
            Long tipoEquipoId = repuestoUpdateDto.getTipoEquipoId() != null
                    ? repuestoUpdateDto.getTipoEquipoId()
                    : repuesto.getTipoEquipo().getId();

            if (!repuesto.getDescripcion().equals(repuestoUpdateDto.getDescripcion()) &&
                    repuestoRepository.existsByDescripcionAndTipoEquipoIdAndIdNot(
                            repuestoUpdateDto.getDescripcion(), tipoEquipoId, id)) {
                throw new RepuestoAlreadyExistsException(
                        "Ya existe un repuesto con la misma descripción para este tipo de equipo");
            }
            repuesto.setDescripcion(repuestoUpdateDto.getDescripcion());
        }

        Repuesto repuestoActualizado = repuestoRepository.save(repuesto);

        return convertirARepuestoResponseDto(repuestoActualizado);
    }

    @Override
    public void eliminarRepuesto(Long id) {
        if (!repuestoRepository.existsById(id)) {
            throw new RepuestoNotFoundException("Repuesto no encontrado con ID: " + id);
        }
        repuestoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeRepuesto(String descripcion, Long tipoEquipoId) {
        return repuestoRepository.existsByDescripcionAndTipoEquipoId(descripcion, tipoEquipoId);
    }

    private RepuestoResponseDto convertirARepuestoResponseDto(Repuesto repuesto) {
        return new RepuestoResponseDto(
                repuesto.getId(),
                repuesto.getDescripcion(),
                repuesto.getTipoEquipo() != null ? repuesto.getTipoEquipo().getId() : null,
                repuesto.getTipoEquipo() != null ? repuesto.getTipoEquipo().getDescripcion() : null,
                repuesto.getDescripcionCompleta()
        );
    }

    private RepuestoListDto convertirARepuestoListDto(Repuesto repuesto) {
        return new RepuestoListDto(
                repuesto.getId(),
                repuesto.getDescripcion(),
                repuesto.getTipoEquipo() != null ? repuesto.getTipoEquipo().getId() : null,
                repuesto.getTipoEquipo() != null ? repuesto.getTipoEquipo().getDescripcion() : null,
                repuesto.getDescripcionCompleta()
        );
    }
}
