package com.sigret.services.impl;

import com.sigret.dtos.modelo.ModeloCreateDto;
import com.sigret.dtos.modelo.ModeloListDto;
import com.sigret.dtos.modelo.ModeloResponseDto;
import com.sigret.dtos.modelo.ModeloUpdateDto;
import com.sigret.entities.Marca;
import com.sigret.entities.Modelo;
import com.sigret.exception.ModeloAlreadyExistsException;
import com.sigret.exception.ModeloNotFoundException;
import com.sigret.repositories.MarcaRepository;
import com.sigret.repositories.ModeloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModeloServiceImplTest {

    @Mock
    private ModeloRepository modeloRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @InjectMocks
    private ModeloServiceImpl modeloService;

    private Marca marca;
    private Modelo modelo;

    @BeforeEach
    void setUp() {
        marca = new Marca();
        marca.setId(1L);
        marca.setDescripcion("Samsung");

        modelo = new Modelo();
        modelo.setId(1L);
        modelo.setDescripcion("Galaxy S24");
        modelo.setMarca(marca);
    }

    @Test
    void crearModelo_conDatosValidos_retornaModeloResponseDto() {
        ModeloCreateDto createDto = new ModeloCreateDto();
        createDto.setDescripcion("Galaxy S24");
        createDto.setMarcaId(1L);

        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));
        when(modeloRepository.existsByDescripcionAndMarcaId("Galaxy S24", 1L)).thenReturn(false);
        when(modeloRepository.save(any(Modelo.class))).thenReturn(modelo);

        ModeloResponseDto resultado = modeloService.crearModelo(createDto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Galaxy S24", resultado.getDescripcion());
        assertEquals(1L, resultado.getMarcaId());
        assertEquals("Samsung", resultado.getMarcaDescripcion());
    }

    @Test
    void crearModelo_conMarcaInexistente_lanzaRuntimeException() {
        ModeloCreateDto createDto = new ModeloCreateDto();
        createDto.setDescripcion("Galaxy S24");
        createDto.setMarcaId(99L);

        when(marcaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> modeloService.crearModelo(createDto));
    }

    @Test
    void crearModelo_conDescripcionDuplicadaEnMarca_lanzaModeloAlreadyExistsException() {
        ModeloCreateDto createDto = new ModeloCreateDto();
        createDto.setDescripcion("Galaxy S24");
        createDto.setMarcaId(1L);

        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));
        when(modeloRepository.existsByDescripcionAndMarcaId("Galaxy S24", 1L)).thenReturn(true);

        assertThrows(ModeloAlreadyExistsException.class, () -> modeloService.crearModelo(createDto));
    }

    @Test
    void obtenerModeloPorId_conIdExistente_retornaModeloResponseDto() {
        when(modeloRepository.findById(1L)).thenReturn(Optional.of(modelo));

        ModeloResponseDto resultado = modeloService.obtenerModeloPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Galaxy S24", resultado.getDescripcion());
    }

    @Test
    void obtenerModeloPorId_conIdInexistente_lanzaModeloNotFoundException() {
        when(modeloRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ModeloNotFoundException.class, () -> modeloService.obtenerModeloPorId(99L));
    }

    @Test
    void obtenerModelos_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Modelo> page = new PageImpl<>(List.of(modelo));
        when(modeloRepository.findAll(pageable)).thenReturn(page);

        Page<ModeloListDto> resultado = modeloService.obtenerModelos(pageable);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void obtenerTodosLosModelos_retornaLista() {
        when(modeloRepository.findAll()).thenReturn(List.of(modelo));

        List<ModeloListDto> resultado = modeloService.obtenerTodosLosModelos();

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerModelosPorMarca_retornaListaFiltrada() {
        when(modeloRepository.findByMarcaId(1L)).thenReturn(List.of(modelo));

        List<ModeloListDto> resultado = modeloService.obtenerModelosPorMarca(1L);

        assertEquals(1, resultado.size());
        assertEquals("Galaxy S24", resultado.get(0).getDescripcion());
    }

    @Test
    void buscarModelos_conTermino_retornaResultados() {
        when(modeloRepository.buscarPorTermino("Galaxy")).thenReturn(List.of(modelo));

        List<ModeloListDto> resultado = modeloService.buscarModelos("Galaxy");

        assertEquals(1, resultado.size());
    }

    @Test
    void actualizarModelo_conDatosValidos_retornaModeloActualizado() {
        ModeloUpdateDto updateDto = new ModeloUpdateDto();
        updateDto.setDescripcion("Galaxy S25");

        Modelo modeloActualizado = new Modelo();
        modeloActualizado.setId(1L);
        modeloActualizado.setDescripcion("Galaxy S25");
        modeloActualizado.setMarca(marca);

        when(modeloRepository.findById(1L)).thenReturn(Optional.of(modelo));
        when(modeloRepository.existsByDescripcionAndMarcaId("Galaxy S25", 1L)).thenReturn(false);
        when(modeloRepository.save(any(Modelo.class))).thenReturn(modeloActualizado);

        ModeloResponseDto resultado = modeloService.actualizarModelo(1L, updateDto);

        assertNotNull(resultado);
        assertEquals("Galaxy S25", resultado.getDescripcion());
    }

    @Test
    void actualizarModelo_conDescripcionDuplicada_lanzaModeloAlreadyExistsException() {
        ModeloUpdateDto updateDto = new ModeloUpdateDto();
        updateDto.setDescripcion("Otro Modelo");

        when(modeloRepository.findById(1L)).thenReturn(Optional.of(modelo));
        when(modeloRepository.existsByDescripcionAndMarcaId("Otro Modelo", 1L)).thenReturn(true);

        assertThrows(ModeloAlreadyExistsException.class, () -> modeloService.actualizarModelo(1L, updateDto));
    }

    @Test
    void eliminarModelo_conIdExistente_eliminaModelo() {
        when(modeloRepository.existsById(1L)).thenReturn(true);

        modeloService.eliminarModelo(1L);

        verify(modeloRepository).deleteById(1L);
    }

    @Test
    void eliminarModelo_conIdInexistente_lanzaModeloNotFoundException() {
        when(modeloRepository.existsById(99L)).thenReturn(false);

        assertThrows(ModeloNotFoundException.class, () -> modeloService.eliminarModelo(99L));
    }

    @Test
    void existeModeloConDescripcionYMarca_retornaTrue() {
        when(modeloRepository.existsByDescripcionAndMarcaId("Galaxy S24", 1L)).thenReturn(true);

        assertTrue(modeloService.existeModeloConDescripcionYMarca("Galaxy S24", 1L));
    }
}
