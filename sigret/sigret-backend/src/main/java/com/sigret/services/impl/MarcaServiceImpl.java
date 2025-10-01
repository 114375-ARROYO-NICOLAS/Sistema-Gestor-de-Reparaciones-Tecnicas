package com.sigret.services.impl;

import com.sigret.dtos.marca.MarcaCreateDto;
import com.sigret.dtos.marca.MarcaListDto;
import com.sigret.dtos.marca.MarcaResponseDto;
import com.sigret.dtos.marca.MarcaUpdateDto;
import com.sigret.entities.Marca;
import com.sigret.exception.MarcaNotFoundException;
import com.sigret.exception.MarcaAlreadyExistsException;
import com.sigret.repositories.MarcaRepository;
import com.sigret.services.MarcaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MarcaServiceImpl implements MarcaService {

    @Autowired
    private MarcaRepository marcaRepository;

    @Override
    public MarcaResponseDto crearMarca(MarcaCreateDto marcaCreateDto) {
        // Validar que la marca no existe
        if (existeMarcaConDescripcion(marcaCreateDto.getDescripcion())) {
            throw new MarcaAlreadyExistsException("Ya existe una marca con la descripción: " + marcaCreateDto.getDescripcion());
        }

        // Crear la marca
        Marca marca = new Marca();
        marca.setDescripcion(marcaCreateDto.getDescripcion());

        Marca marcaGuardada = marcaRepository.save(marca);

        return convertirAMarcaResponseDto(marcaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public MarcaResponseDto obtenerMarcaPorId(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new MarcaNotFoundException("Marca no encontrada con ID: " + id));

        return convertirAMarcaResponseDto(marca);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MarcaListDto> obtenerMarcas(Pageable pageable) {
        Page<Marca> marcas = marcaRepository.findAll(pageable);
        return marcas.map(this::convertirAMarcaListDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcaListDto> obtenerTodasLasMarcas() {
        List<Marca> marcas = marcaRepository.findAll();
        return marcas.stream()
                .map(this::convertirAMarcaListDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcaListDto> buscarMarcas(String termino) {
        List<Marca> marcas = marcaRepository.buscarPorTermino(termino);
        return marcas.stream()
                .map(this::convertirAMarcaListDto)
                .collect(Collectors.toList());
    }

    @Override
    public MarcaResponseDto actualizarMarca(Long id, MarcaUpdateDto marcaUpdateDto) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new MarcaNotFoundException("Marca no encontrada con ID: " + id));

        // Actualizar descripción si se proporciona
        if (marcaUpdateDto.getDescripcion() != null) {
            // Validar que la nueva descripción no existe en otra marca
            if (!marca.getDescripcion().equals(marcaUpdateDto.getDescripcion()) &&
                existeMarcaConDescripcion(marcaUpdateDto.getDescripcion())) {
                throw new MarcaAlreadyExistsException("Ya existe una marca con la descripción: " + marcaUpdateDto.getDescripcion());
            }
            marca.setDescripcion(marcaUpdateDto.getDescripcion());
        }

        Marca marcaActualizada = marcaRepository.save(marca);

        return convertirAMarcaResponseDto(marcaActualizada);
    }

    @Override
    public void eliminarMarca(Long id) {
        if (!marcaRepository.existsById(id)) {
            throw new MarcaNotFoundException("Marca no encontrada con ID: " + id);
        }
        marcaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeMarcaConDescripcion(String descripcion) {
        return marcaRepository.existsByDescripcion(descripcion);
    }

    private MarcaResponseDto convertirAMarcaResponseDto(Marca marca) {
        return new MarcaResponseDto(
                marca.getId(),
                marca.getDescripcion()
        );
    }

    private MarcaListDto convertirAMarcaListDto(Marca marca) {
        return new MarcaListDto(
                marca.getId(),
                marca.getDescripcion()
        );
    }
}
