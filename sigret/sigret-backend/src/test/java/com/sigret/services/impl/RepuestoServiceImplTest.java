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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepuestoServiceImplTest {

    @Mock
    private RepuestoRepository repuestoRepository;

    @Mock
    private TipoEquipoRepository tipoEquipoRepository;

    @InjectMocks
    private RepuestoServiceImpl repuestoService;

    private TipoEquipo tipoEquipo;
    private Repuesto repuesto;

    @BeforeEach
    void setUp() {
        tipoEquipo = new TipoEquipo();
        tipoEquipo.setId(1L);
        tipoEquipo.setDescripcion("Notebook");

        repuesto = new Repuesto();
        repuesto.setId(1L);
        repuesto.setDescripcion("Pantalla LCD");
        repuesto.setTipoEquipo(tipoEquipo);
    }

    @Test
    void crearRepuesto_conDatosValidos_retornaRepuestoResponseDto() {
        RepuestoCreateDto createDto = new RepuestoCreateDto();
        createDto.setDescripcion("Pantalla LCD");
        createDto.setTipoEquipoId(1L);

        when(tipoEquipoRepository.findById(1L)).thenReturn(Optional.of(tipoEquipo));
        when(repuestoRepository.existsByDescripcionAndTipoEquipoId("Pantalla LCD", 1L)).thenReturn(false);
        when(repuestoRepository.save(any(Repuesto.class))).thenReturn(repuesto);

        RepuestoResponseDto resultado = repuestoService.crearRepuesto(createDto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Pantalla LCD", resultado.getDescripcion());
    }

    @Test
    void crearRepuesto_conTipoEquipoInexistente_lanzaTipoEquipoNotFoundException() {
        RepuestoCreateDto createDto = new RepuestoCreateDto();
        createDto.setDescripcion("Pantalla LCD");
        createDto.setTipoEquipoId(99L);

        when(tipoEquipoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TipoEquipoNotFoundException.class, () -> repuestoService.crearRepuesto(createDto));
    }

    @Test
    void crearRepuesto_conDescripcionDuplicada_lanzaRepuestoAlreadyExistsException() {
        RepuestoCreateDto createDto = new RepuestoCreateDto();
        createDto.setDescripcion("Pantalla LCD");
        createDto.setTipoEquipoId(1L);

        when(tipoEquipoRepository.findById(1L)).thenReturn(Optional.of(tipoEquipo));
        when(repuestoRepository.existsByDescripcionAndTipoEquipoId("Pantalla LCD", 1L)).thenReturn(true);

        assertThrows(RepuestoAlreadyExistsException.class, () -> repuestoService.crearRepuesto(createDto));
    }

    @Test
    void obtenerRepuestoPorId_conIdExistente_retornaRepuestoResponseDto() {
        when(repuestoRepository.findById(1L)).thenReturn(Optional.of(repuesto));

        RepuestoResponseDto resultado = repuestoService.obtenerRepuestoPorId(1L);

        assertNotNull(resultado);
        assertEquals("Pantalla LCD", resultado.getDescripcion());
    }

    @Test
    void obtenerRepuestoPorId_conIdInexistente_lanzaRepuestoNotFoundException() {
        when(repuestoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RepuestoNotFoundException.class, () -> repuestoService.obtenerRepuestoPorId(99L));
    }

    @Test
    void obtenerTodosLosRepuestos_retornaLista() {
        when(repuestoRepository.findAll()).thenReturn(List.of(repuesto));

        List<RepuestoListDto> resultado = repuestoService.obtenerTodosLosRepuestos();

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerRepuestosPorTipoEquipo_retornaListaFiltrada() {
        when(repuestoRepository.findByTipoEquipoId(1L)).thenReturn(List.of(repuesto));

        List<RepuestoListDto> resultado = repuestoService.obtenerRepuestosPorTipoEquipo(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarRepuestos_conTermino_retornaResultados() {
        when(repuestoRepository.buscarPorTermino("Pantalla")).thenReturn(List.of(repuesto));

        List<RepuestoListDto> resultado = repuestoService.buscarRepuestos("Pantalla");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarRepuestos_conTerminoNull_retornaTodos() {
        when(repuestoRepository.findAll()).thenReturn(List.of(repuesto));

        List<RepuestoListDto> resultado = repuestoService.buscarRepuestos(null);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarRepuestos_conTerminoVacio_retornaTodos() {
        when(repuestoRepository.findAll()).thenReturn(List.of(repuesto));

        List<RepuestoListDto> resultado = repuestoService.buscarRepuestos("  ");

        assertEquals(1, resultado.size());
    }

    @Test
    void actualizarRepuesto_conDatosValidos_retornaRepuestoActualizado() {
        RepuestoUpdateDto updateDto = new RepuestoUpdateDto();
        updateDto.setDescripcion("Pantalla OLED");

        when(repuestoRepository.findById(1L)).thenReturn(Optional.of(repuesto));
        when(repuestoRepository.existsByDescripcionAndTipoEquipoIdAndIdNot("Pantalla OLED", 1L, 1L)).thenReturn(false);
        when(repuestoRepository.save(any(Repuesto.class))).thenReturn(repuesto);

        RepuestoResponseDto resultado = repuestoService.actualizarRepuesto(1L, updateDto);

        assertNotNull(resultado);
    }

    @Test
    void actualizarRepuesto_conIdInexistente_lanzaRepuestoNotFoundException() {
        RepuestoUpdateDto updateDto = new RepuestoUpdateDto();
        when(repuestoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RepuestoNotFoundException.class, () -> repuestoService.actualizarRepuesto(99L, updateDto));
    }

    @Test
    void actualizarRepuesto_conDescripcionDuplicada_lanzaRepuestoAlreadyExistsException() {
        RepuestoUpdateDto updateDto = new RepuestoUpdateDto();
        updateDto.setDescripcion("Teclado");

        when(repuestoRepository.findById(1L)).thenReturn(Optional.of(repuesto));
        when(repuestoRepository.existsByDescripcionAndTipoEquipoIdAndIdNot("Teclado", 1L, 1L)).thenReturn(true);

        assertThrows(RepuestoAlreadyExistsException.class, () -> repuestoService.actualizarRepuesto(1L, updateDto));
    }

    @Test
    void eliminarRepuesto_conIdExistente_eliminaRepuesto() {
        when(repuestoRepository.existsById(1L)).thenReturn(true);

        repuestoService.eliminarRepuesto(1L);

        verify(repuestoRepository).deleteById(1L);
    }

    @Test
    void eliminarRepuesto_conIdInexistente_lanzaRepuestoNotFoundException() {
        when(repuestoRepository.existsById(99L)).thenReturn(false);

        assertThrows(RepuestoNotFoundException.class, () -> repuestoService.eliminarRepuesto(99L));
    }

    @Test
    void existeRepuesto_retornaTrue() {
        when(repuestoRepository.existsByDescripcionAndTipoEquipoId("Pantalla LCD", 1L)).thenReturn(true);

        assertTrue(repuestoService.existeRepuesto("Pantalla LCD", 1L));
    }
}
