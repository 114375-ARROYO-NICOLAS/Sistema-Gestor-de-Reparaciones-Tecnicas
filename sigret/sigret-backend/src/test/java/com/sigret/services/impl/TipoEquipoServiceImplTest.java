package com.sigret.services.impl;

import com.sigret.dtos.tipoEquipo.TipoEquipoCreateDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoListDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoResponseDto;
import com.sigret.dtos.tipoEquipo.TipoEquipoUpdateDto;
import com.sigret.entities.TipoEquipo;
import com.sigret.exception.TipoEquipoNotFoundException;
import com.sigret.repositories.TipoEquipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoEquipoServiceImplTest {

    @Mock
    private TipoEquipoRepository tipoEquipoRepository;

    @InjectMocks
    private TipoEquipoServiceImpl tipoEquipoService;

    private TipoEquipo tipoEquipo;

    @BeforeEach
    void setUp() {
        tipoEquipo = new TipoEquipo();
        tipoEquipo.setId(1L);
        tipoEquipo.setDescripcion("Notebook");
    }

    @Test
    void crearTipoEquipo_conDatosValidos_retornaTipoEquipoResponseDto() {
        TipoEquipoCreateDto createDto = new TipoEquipoCreateDto();
        createDto.setDescripcion("Notebook");

        when(tipoEquipoRepository.save(any(TipoEquipo.class))).thenReturn(tipoEquipo);

        TipoEquipoResponseDto resultado = tipoEquipoService.crearTipoEquipo(createDto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Notebook", resultado.getDescripcion());
    }

    @Test
    void obtenerTipoEquipoPorId_conIdExistente_retornaTipoEquipoResponseDto() {
        when(tipoEquipoRepository.findById(1L)).thenReturn(Optional.of(tipoEquipo));

        TipoEquipoResponseDto resultado = tipoEquipoService.obtenerTipoEquipoPorId(1L);

        assertNotNull(resultado);
        assertEquals("Notebook", resultado.getDescripcion());
    }

    @Test
    void obtenerTipoEquipoPorId_conIdInexistente_lanzaTipoEquipoNotFoundException() {
        when(tipoEquipoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TipoEquipoNotFoundException.class, () -> tipoEquipoService.obtenerTipoEquipoPorId(99L));
    }

    @Test
    void obtenerTodosLosTiposEquipo_retornaLista() {
        TipoEquipo tipoEquipo2 = new TipoEquipo();
        tipoEquipo2.setId(2L);
        tipoEquipo2.setDescripcion("Celular");

        when(tipoEquipoRepository.findAll()).thenReturn(Arrays.asList(tipoEquipo, tipoEquipo2));

        List<TipoEquipoListDto> resultado = tipoEquipoService.obtenerTodosLosTiposEquipo();

        assertEquals(2, resultado.size());
    }

    @Test
    void actualizarTipoEquipo_conDatosValidos_retornaTipoEquipoActualizado() {
        TipoEquipoUpdateDto updateDto = new TipoEquipoUpdateDto();
        updateDto.setDescripcion("Laptop");

        TipoEquipo tipoEquipoActualizado = new TipoEquipo();
        tipoEquipoActualizado.setId(1L);
        tipoEquipoActualizado.setDescripcion("Laptop");

        when(tipoEquipoRepository.findById(1L)).thenReturn(Optional.of(tipoEquipo));
        when(tipoEquipoRepository.save(any(TipoEquipo.class))).thenReturn(tipoEquipoActualizado);

        TipoEquipoResponseDto resultado = tipoEquipoService.actualizarTipoEquipo(1L, updateDto);

        assertNotNull(resultado);
        assertEquals("Laptop", resultado.getDescripcion());
    }

    @Test
    void actualizarTipoEquipo_conIdInexistente_lanzaTipoEquipoNotFoundException() {
        TipoEquipoUpdateDto updateDto = new TipoEquipoUpdateDto();
        updateDto.setDescripcion("Laptop");

        when(tipoEquipoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TipoEquipoNotFoundException.class, () -> tipoEquipoService.actualizarTipoEquipo(99L, updateDto));
    }

    @Test
    void eliminarTipoEquipo_conIdExistente_eliminaTipoEquipo() {
        when(tipoEquipoRepository.existsById(1L)).thenReturn(true);

        tipoEquipoService.eliminarTipoEquipo(1L);

        verify(tipoEquipoRepository).deleteById(1L);
    }

    @Test
    void eliminarTipoEquipo_conIdInexistente_lanzaTipoEquipoNotFoundException() {
        when(tipoEquipoRepository.existsById(99L)).thenReturn(false);

        assertThrows(TipoEquipoNotFoundException.class, () -> tipoEquipoService.eliminarTipoEquipo(99L));
    }
}
