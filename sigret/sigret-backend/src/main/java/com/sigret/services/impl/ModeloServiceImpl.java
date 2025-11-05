package com.sigret.services.impl;

import com.sigret.dtos.modelo.ModeloCreateDto;
import com.sigret.dtos.modelo.ModeloListDto;
import com.sigret.dtos.modelo.ModeloResponseDto;
import com.sigret.dtos.modelo.ModeloUpdateDto;
import com.sigret.entities.Marca;
import com.sigret.entities.Modelo;
import com.sigret.exception.ModeloNotFoundException;
import com.sigret.exception.ModeloAlreadyExistsException;
import com.sigret.repositories.MarcaRepository;
import com.sigret.repositories.ModeloRepository;
import com.sigret.services.ModeloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ModeloServiceImpl implements ModeloService {

    @Autowired
    private ModeloRepository modeloRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Override
    public ModeloResponseDto crearModelo(ModeloCreateDto modeloCreateDto) {
        // Validar que la marca existe
        Marca marca = marcaRepository.findById(modeloCreateDto.getMarcaId())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + modeloCreateDto.getMarcaId()));

        // Validar que el modelo no existe para esta marca
        if (existeModeloConDescripcionYMarca(modeloCreateDto.getDescripcion(), modeloCreateDto.getMarcaId())) {
            throw new ModeloAlreadyExistsException("Ya existe un modelo con la descripción '" + 
                modeloCreateDto.getDescripcion() + "' para la marca seleccionada");
        }

        // Crear el modelo
        Modelo modelo = new Modelo();
        modelo.setDescripcion(modeloCreateDto.getDescripcion());
        modelo.setMarca(marca);

        Modelo modeloGuardado = modeloRepository.save(modelo);

        return convertirAModeloResponseDto(modeloGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ModeloResponseDto obtenerModeloPorId(Long id) {
        Modelo modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new ModeloNotFoundException("Modelo no encontrado con ID: " + id));

        return convertirAModeloResponseDto(modelo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModeloListDto> obtenerModelos(Pageable pageable) {
        Page<Modelo> modelos = modeloRepository.findAll(pageable);
        return modelos.map(this::convertirAModeloListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModeloListDto> obtenerTodosLosModelos() {
        List<Modelo> modelos = modeloRepository.findAll();
        return modelos.stream()
                .map(this::convertirAModeloListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModeloListDto> obtenerModelosPorMarca(Long marcaId) {
        List<Modelo> modelos = modeloRepository.findByMarcaId(marcaId);
        return modelos.stream()
                .map(this::convertirAModeloListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModeloListDto> buscarModelos(String termino) {
        List<Modelo> modelos = modeloRepository.buscarPorTermino(termino);
        return modelos.stream()
                .map(this::convertirAModeloListDto)
                .collect(Collectors.toList());
    }

    @Override
    public ModeloResponseDto actualizarModelo(Long id, ModeloUpdateDto modeloUpdateDto) {
        Modelo modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new ModeloNotFoundException("Modelo no encontrado con ID: " + id));

        // Actualizar marca si se proporciona
        if (modeloUpdateDto.getMarcaId() != null) {
            Marca marca = marcaRepository.findById(modeloUpdateDto.getMarcaId())
                    .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + modeloUpdateDto.getMarcaId()));
            modelo.setMarca(marca);
        }

        // Actualizar descripción si se proporciona
        if (modeloUpdateDto.getDescripcion() != null) {
            // Validar que la nueva descripción no existe para la marca
            Long marcaId = modeloUpdateDto.getMarcaId() != null ? modeloUpdateDto.getMarcaId() : modelo.getMarca().getId();
            if (!modelo.getDescripcion().equals(modeloUpdateDto.getDescripcion()) &&
                existeModeloConDescripcionYMarca(modeloUpdateDto.getDescripcion(), marcaId)) {
                throw new ModeloAlreadyExistsException("Ya existe un modelo con la descripción '" + 
                    modeloUpdateDto.getDescripcion() + "' para la marca seleccionada");
            }
            modelo.setDescripcion(modeloUpdateDto.getDescripcion());
        }

        Modelo modeloActualizado = modeloRepository.save(modelo);

        return convertirAModeloResponseDto(modeloActualizado);
    }

    @Override
    public void eliminarModelo(Long id) {
        if (!modeloRepository.existsById(id)) {
            throw new ModeloNotFoundException("Modelo no encontrado con ID: " + id);
        }
        modeloRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeModeloConDescripcionYMarca(String descripcion, Long marcaId) {
        return modeloRepository.existsByDescripcionAndMarcaId(descripcion, marcaId);
    }

    private ModeloResponseDto convertirAModeloResponseDto(Modelo modelo) {
        return new ModeloResponseDto(
                modelo.getId(),
                modelo.getDescripcion(),
                modelo.getMarca().getId(),
                modelo.getMarca().getDescripcion()
        );
    }

    private ModeloListDto convertirAModeloListDto(Modelo modelo) {
        return new ModeloListDto(
                modelo.getId(),
                modelo.getDescripcion(),
                modelo.getMarca().getId(),
                modelo.getMarca().getDescripcion()
        );
    }
}
