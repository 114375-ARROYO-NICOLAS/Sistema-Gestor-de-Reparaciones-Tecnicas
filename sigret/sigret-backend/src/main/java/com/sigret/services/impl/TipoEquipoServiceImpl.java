package com.sigret.services.impl;

import com.sigret.dtos.tipoEquipo.TipoEquipoCreateDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoListDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoResponseDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoUpdateDto;
import com.sigret.entities.TipoEquipo;
import com.sigret.exception.TipoEquipoNotFoundException;
import com.sigret.repositories.TipoEquipoRepository;
import com.sigret.services.TipoEquipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TipoEquipoServiceImpl implements TipoEquipoService {

    @Autowired
    private TipoEquipoRepository tipoEquipoRepository;

    @Override
    public TipoEquipoResponseDto crearTipoEquipo(TipoEquipoCreateDto tipoEquipoCreateDto) {
        TipoEquipo tipoEquipo = new TipoEquipo();
        tipoEquipo.setDescripcion(tipoEquipoCreateDto.getDescripcion());

        TipoEquipo tipoEquipoGuardado = tipoEquipoRepository.save(tipoEquipo);
        return convertirATipoEquipoResponseDto(tipoEquipoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoEquipoResponseDto obtenerTipoEquipoPorId(Long id) {
        TipoEquipo tipoEquipo = tipoEquipoRepository.findById(id)
                .orElseThrow(() -> new TipoEquipoNotFoundException("Tipo de equipo no encontrado con ID: " + id));
        return convertirATipoEquipoResponseDto(tipoEquipo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoEquipoListDto> obtenerTodosLosTiposEquipo() {
        return tipoEquipoRepository.findAll().stream()
                .map(this::convertirATipoEquipoListDto)
                .collect(Collectors.toList());
    }

    @Override
    public TipoEquipoResponseDto actualizarTipoEquipo(Long id, TipoEquipoUpdateDto tipoEquipoUpdateDto) {
        TipoEquipo tipoEquipo = tipoEquipoRepository.findById(id)
                .orElseThrow(() -> new TipoEquipoNotFoundException("Tipo de equipo no encontrado con ID: " + id));

        tipoEquipo.setDescripcion(tipoEquipoUpdateDto.getDescripcion());

        TipoEquipo tipoEquipoActualizado = tipoEquipoRepository.save(tipoEquipo);
        return convertirATipoEquipoResponseDto(tipoEquipoActualizado);
    }

    @Override
    public void eliminarTipoEquipo(Long id) {
        if (!tipoEquipoRepository.existsById(id)) {
            throw new TipoEquipoNotFoundException("Tipo de equipo no encontrado con ID: " + id);
        }
        tipoEquipoRepository.deleteById(id);
    }

    // Métodos de conversión
    private TipoEquipoResponseDto convertirATipoEquipoResponseDto(TipoEquipo tipoEquipo) {
        return new TipoEquipoResponseDto(
                tipoEquipo.getId(),
                tipoEquipo.getDescripcion()
        );
    }

    private TipoEquipoListDto convertirATipoEquipoListDto(TipoEquipo tipoEquipo) {
        return new TipoEquipoListDto(
                tipoEquipo.getId(),
                tipoEquipo.getDescripcion()
        );
    }
}
