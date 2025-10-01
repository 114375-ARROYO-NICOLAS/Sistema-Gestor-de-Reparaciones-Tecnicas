package com.sigret.services.impl;

import com.sigret.dtos.equipo.EquipoCreateDto;
import com.sigret.dtos.equipo.EquipoListDto;
import com.sigret.dtos.equipo.EquipoResponseDto;
import com.sigret.dtos.equipo.EquipoUpdateDto;
import com.sigret.entities.Equipo;
import com.sigret.entities.Marca;
import com.sigret.entities.Modelo;
import com.sigret.entities.TipoEquipo;
import com.sigret.exception.EquipoNotFoundException;
import com.sigret.exception.NumeroSerieAlreadyExistsException;
import com.sigret.repositories.EquipoRepository;
import com.sigret.repositories.MarcaRepository;
import com.sigret.repositories.ModeloRepository;
import com.sigret.repositories.TipoEquipoRepository;
import com.sigret.services.EquipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipoServiceImpl implements EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private ModeloRepository modeloRepository;

    @Autowired
    private TipoEquipoRepository tipoEquipoRepository;

    @Override
    public EquipoResponseDto crearEquipo(EquipoCreateDto equipoCreateDto) {
        // Validar que el número de serie no existe (si se proporciona)
        if (equipoCreateDto.getNumeroSerie() != null && 
            !equipoCreateDto.getNumeroSerie().trim().isEmpty() &&
            existeEquipoConNumeroSerie(equipoCreateDto.getNumeroSerie())) {
            throw new NumeroSerieAlreadyExistsException("Ya existe un equipo con el número de serie: " + equipoCreateDto.getNumeroSerie());
        }

        // Obtener entidades relacionadas
        TipoEquipo tipoEquipo = tipoEquipoRepository.findById(equipoCreateDto.getTipoEquipoId())
                .orElseThrow(() -> new RuntimeException("Tipo de equipo no encontrado"));
        
        Marca marca = marcaRepository.findById(equipoCreateDto.getMarcaId())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));

        Modelo modelo = null;
        if (equipoCreateDto.getModeloId() != null) {
            modelo = modeloRepository.findById(equipoCreateDto.getModeloId())
                    .orElseThrow(() -> new RuntimeException("Modelo no encontrado"));
        }

        // Crear el equipo
        Equipo equipo = new Equipo();
        equipo.setTipoEquipo(tipoEquipo);
        equipo.setMarca(marca);
        equipo.setModelo(modelo);
        equipo.setNumeroSerie(equipoCreateDto.getNumeroSerie());
        equipo.setColor(equipoCreateDto.getColor());
        equipo.setObservaciones(equipoCreateDto.getObservaciones());

        Equipo equipoGuardado = equipoRepository.save(equipo);

        return convertirAEquipoResponseDto(equipoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipoResponseDto obtenerEquipoPorId(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoNotFoundException("Equipo no encontrado con ID: " + id));

        return convertirAEquipoResponseDto(equipo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EquipoListDto> obtenerEquipos(Pageable pageable) {
        Page<Equipo> equipos = equipoRepository.findAll(pageable);
        return equipos.map(this::convertirAEquipoListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipoListDto> obtenerTodosLosEquipos() {
        List<Equipo> equipos = equipoRepository.findAll();
        return equipos.stream()
                .map(this::convertirAEquipoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipoListDto> buscarEquipos(String termino) {
        List<Equipo> equipos = equipoRepository.findAll().stream()
                .filter(e -> e.getDescripcionCompleta().toLowerCase().contains(termino.toLowerCase()) ||
                           (e.getNumeroSerie() != null && e.getNumeroSerie().contains(termino)))
                .collect(Collectors.toList());

        return equipos.stream()
                .map(this::convertirAEquipoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipoListDto> obtenerEquiposPorMarca(Long marcaId) {
        List<Equipo> equipos = equipoRepository.findByMarcaId(marcaId);
        return equipos.stream()
                .map(this::convertirAEquipoListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipoListDto> obtenerEquiposPorTipo(Long tipoEquipoId) {
        List<Equipo> equipos = equipoRepository.findByTipoEquipoId(tipoEquipoId);
        return equipos.stream()
                .map(this::convertirAEquipoListDto)
                .collect(Collectors.toList());
    }

    @Override
    public EquipoResponseDto actualizarEquipo(Long id, EquipoUpdateDto equipoUpdateDto) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoNotFoundException("Equipo no encontrado con ID: " + id));

        // Actualizar campos si se proporcionan
        if (equipoUpdateDto.getTipoEquipoId() != null) {
            TipoEquipo tipoEquipo = tipoEquipoRepository.findById(equipoUpdateDto.getTipoEquipoId())
                    .orElseThrow(() -> new RuntimeException("Tipo de equipo no encontrado"));
            equipo.setTipoEquipo(tipoEquipo);
        }

        if (equipoUpdateDto.getMarcaId() != null) {
            Marca marca = marcaRepository.findById(equipoUpdateDto.getMarcaId())
                    .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
            equipo.setMarca(marca);
        }

        if (equipoUpdateDto.getModeloId() != null) {
            Modelo modelo = modeloRepository.findById(equipoUpdateDto.getModeloId())
                    .orElseThrow(() -> new RuntimeException("Modelo no encontrado"));
            equipo.setModelo(modelo);
        }

        if (equipoUpdateDto.getNumeroSerie() != null) {
            equipo.setNumeroSerie(equipoUpdateDto.getNumeroSerie());
        }

        if (equipoUpdateDto.getColor() != null) {
            equipo.setColor(equipoUpdateDto.getColor());
        }

        if (equipoUpdateDto.getObservaciones() != null) {
            equipo.setObservaciones(equipoUpdateDto.getObservaciones());
        }

        Equipo equipoActualizado = equipoRepository.save(equipo);

        return convertirAEquipoResponseDto(equipoActualizado);
    }

    @Override
    public void eliminarEquipo(Long id) {
        if (!equipoRepository.existsById(id)) {
            throw new EquipoNotFoundException("Equipo no encontrado con ID: " + id);
        }
        equipoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEquipoConNumeroSerie(String numeroSerie) {
        return equipoRepository.existsByNumeroSerie(numeroSerie);
    }

    private EquipoResponseDto convertirAEquipoResponseDto(Equipo equipo) {
        return new EquipoResponseDto(
                equipo.getId(),
                equipo.getDescripcionCompleta(),
                equipo.getNumeroSerie(),
                equipo.getColor(),
                equipo.getObservaciones(),
                equipo.getTipoEquipo().getDescripcion(),
                equipo.getMarca().getDescripcion(),
                equipo.getModelo() != null ? equipo.getModelo().getDescripcion() : null
        );
    }

    private EquipoListDto convertirAEquipoListDto(Equipo equipo) {
        return new EquipoListDto(
                equipo.getId(),
                equipo.getDescripcionCompleta(),
                equipo.getNumeroSerie(),
                equipo.getColor(),
                equipo.getTipoEquipo().getDescripcion(),
                equipo.getMarca().getDescripcion(),
                equipo.getModelo() != null ? equipo.getModelo().getDescripcion() : null
        );
    }
}
