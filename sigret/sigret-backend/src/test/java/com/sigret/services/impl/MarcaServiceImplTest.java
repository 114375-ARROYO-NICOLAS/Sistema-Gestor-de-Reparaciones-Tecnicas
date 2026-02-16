package com.sigret.services.impl;

import com.sigret.dtos.marca.MarcaCreateDto;
import com.sigret.dtos.marca.MarcaListDto;
import com.sigret.dtos.marca.MarcaResponseDto;
import com.sigret.dtos.marca.MarcaUpdateDto;
import com.sigret.entities.Marca;
import com.sigret.exception.MarcaAlreadyExistsException;
import com.sigret.exception.MarcaNotFoundException;
import com.sigret.repositories.MarcaRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarcaServiceImplTest {

    @Mock
    private MarcaRepository marcaRepository;

    @InjectMocks
    private MarcaServiceImpl marcaService;

    private Marca marca;

    @BeforeEach
    void setUp() {
        marca = new Marca();
        marca.setId(1L);
        marca.setDescripcion("Samsung");
    }

    @Test
    void crearMarca_conDatosValidos_retornaMarcaResponseDto() {
        MarcaCreateDto createDto = new MarcaCreateDto("Samsung");

        when(marcaRepository.existsByDescripcion("Samsung")).thenReturn(false);
        when(marcaRepository.save(any(Marca.class))).thenReturn(marca);

        MarcaResponseDto resultado = marcaService.crearMarca(createDto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Samsung", resultado.getDescripcion());
        verify(marcaRepository).save(any(Marca.class));
    }

    @Test
    void crearMarca_conDescripcionExistente_lanzaMarcaAlreadyExistsException() {
        MarcaCreateDto createDto = new MarcaCreateDto("Samsung");

        when(marcaRepository.existsByDescripcion("Samsung")).thenReturn(true);

        assertThrows(MarcaAlreadyExistsException.class, () -> marcaService.crearMarca(createDto));
        verify(marcaRepository, never()).save(any(Marca.class));
    }

    @Test
    void obtenerMarcaPorId_conIdExistente_retornaMarcaResponseDto() {
        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));

        MarcaResponseDto resultado = marcaService.obtenerMarcaPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Samsung", resultado.getDescripcion());
    }

    @Test
    void obtenerMarcaPorId_conIdInexistente_lanzaMarcaNotFoundException() {
        when(marcaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MarcaNotFoundException.class, () -> marcaService.obtenerMarcaPorId(99L));
    }

    @Test
    void obtenerMarcas_retornaPaginaDeMarcas() {
        Pageable pageable = PageRequest.of(0, 10);
        Marca marca2 = new Marca();
        marca2.setId(2L);
        marca2.setDescripcion("LG");

        Page<Marca> page = new PageImpl<>(Arrays.asList(marca, marca2));
        when(marcaRepository.findAll(pageable)).thenReturn(page);

        Page<MarcaListDto> resultado = marcaService.obtenerMarcas(pageable);

        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
    }

    @Test
    void obtenerTodasLasMarcas_retornaListaDeMarcas() {
        Marca marca2 = new Marca();
        marca2.setId(2L);
        marca2.setDescripcion("LG");

        when(marcaRepository.findAll()).thenReturn(Arrays.asList(marca, marca2));

        List<MarcaListDto> resultado = marcaService.obtenerTodasLasMarcas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    void buscarMarcas_conTermino_retornaListaFiltrada() {
        when(marcaRepository.buscarPorTermino("Sam")).thenReturn(List.of(marca));

        List<MarcaListDto> resultado = marcaService.buscarMarcas("Sam");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Samsung", resultado.get(0).getDescripcion());
    }

    @Test
    void actualizarMarca_conDatosValidos_retornaMarcaActualizada() {
        MarcaUpdateDto updateDto = new MarcaUpdateDto("Samsung Electronics");

        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));
        when(marcaRepository.existsByDescripcion("Samsung Electronics")).thenReturn(false);

        Marca marcaActualizada = new Marca();
        marcaActualizada.setId(1L);
        marcaActualizada.setDescripcion("Samsung Electronics");
        when(marcaRepository.save(any(Marca.class))).thenReturn(marcaActualizada);

        MarcaResponseDto resultado = marcaService.actualizarMarca(1L, updateDto);

        assertNotNull(resultado);
        assertEquals("Samsung Electronics", resultado.getDescripcion());
    }

    @Test
    void actualizarMarca_conDescripcionDuplicada_lanzaMarcaAlreadyExistsException() {
        MarcaUpdateDto updateDto = new MarcaUpdateDto("LG");

        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));
        when(marcaRepository.existsByDescripcion("LG")).thenReturn(true);

        assertThrows(MarcaAlreadyExistsException.class, () -> marcaService.actualizarMarca(1L, updateDto));
    }

    @Test
    void actualizarMarca_conMismaDescripcion_noValidaDuplicado() {
        MarcaUpdateDto updateDto = new MarcaUpdateDto("Samsung");

        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));
        when(marcaRepository.save(any(Marca.class))).thenReturn(marca);

        MarcaResponseDto resultado = marcaService.actualizarMarca(1L, updateDto);

        assertNotNull(resultado);
        verify(marcaRepository, never()).existsByDescripcion(anyString());
    }

    @Test
    void actualizarMarca_conIdInexistente_lanzaMarcaNotFoundException() {
        MarcaUpdateDto updateDto = new MarcaUpdateDto("Nueva Marca");

        when(marcaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MarcaNotFoundException.class, () -> marcaService.actualizarMarca(99L, updateDto));
    }

    @Test
    void eliminarMarca_conIdExistente_eliminaMarca() {
        when(marcaRepository.existsById(1L)).thenReturn(true);

        marcaService.eliminarMarca(1L);

        verify(marcaRepository).deleteById(1L);
    }

    @Test
    void eliminarMarca_conIdInexistente_lanzaMarcaNotFoundException() {
        when(marcaRepository.existsById(99L)).thenReturn(false);

        assertThrows(MarcaNotFoundException.class, () -> marcaService.eliminarMarca(99L));
        verify(marcaRepository, never()).deleteById(any());
    }

    @Test
    void existeMarcaConDescripcion_conDescripcionExistente_retornaTrue() {
        when(marcaRepository.existsByDescripcion("Samsung")).thenReturn(true);

        assertTrue(marcaService.existeMarcaConDescripcion("Samsung"));
    }

    @Test
    void existeMarcaConDescripcion_conDescripcionInexistente_retornaFalse() {
        when(marcaRepository.existsByDescripcion("Inexistente")).thenReturn(false);

        assertFalse(marcaService.existeMarcaConDescripcion("Inexistente"));
    }
}
