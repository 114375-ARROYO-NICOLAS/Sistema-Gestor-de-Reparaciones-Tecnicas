package com.sigret.services.impl;

import com.sigret.dtos.direccion.DireccionCreateDto;
import com.sigret.dtos.direccion.DireccionListDto;
import com.sigret.dtos.direccion.DireccionResponseDto;
import com.sigret.dtos.direccion.DireccionUpdateDto;
import com.sigret.entities.Direccion;
import com.sigret.entities.Persona;
import com.sigret.entities.TipoDocumento;
import com.sigret.entities.TipoPersona;
import com.sigret.exception.DireccionNotFoundException;
import com.sigret.repositories.DireccionRepository;
import com.sigret.repositories.PersonaRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DireccionServiceImplTest {

    @Mock
    private DireccionRepository direccionRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private DireccionServiceImpl direccionService;

    private Persona persona;
    private Direccion direccion;

    @BeforeEach
    void setUp() {
        TipoPersona tipoPersona = new TipoPersona();
        tipoPersona.setId(1L);
        tipoPersona.setDescripcion("Física");

        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setId(1L);
        tipoDocumento.setDescripcion("DNI");

        persona = new Persona();
        persona.setId(1L);
        persona.setNombre("Juan");
        persona.setApellido("Pérez");
        persona.setTipoPersona(tipoPersona);
        persona.setTipoDocumento(tipoDocumento);

        direccion = new Direccion();
        direccion.setId(1L);
        direccion.setPersona(persona);
        direccion.setCalle("Av. Siempreviva");
        direccion.setNumero("742");
        direccion.setCiudad("Springfield");
        direccion.setProvincia("Buenos Aires");
        direccion.setPais("Argentina");
        direccion.setEsPrincipal(true);
    }

    @Test
    void crearDireccion_conDatosValidos_retornaDireccionResponseDto() {
        DireccionCreateDto createDto = new DireccionCreateDto();
        createDto.setPersonaId(1L);
        createDto.setCalle("Av. Siempreviva");
        createDto.setNumero("742");
        createDto.setCiudad("Springfield");
        createDto.setEsPrincipal(false);

        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(direccionRepository.save(any(Direccion.class))).thenReturn(direccion);

        DireccionResponseDto resultado = direccionService.crearDireccion(createDto);

        assertNotNull(resultado);
        assertEquals("Av. Siempreviva", resultado.getCalle());
        verify(direccionRepository).save(any(Direccion.class));
    }

    @Test
    void crearDireccion_sinPersonaId_lanzaIllegalArgumentException() {
        DireccionCreateDto createDto = new DireccionCreateDto();
        createDto.setPersonaId(null);

        assertThrows(IllegalArgumentException.class, () -> direccionService.crearDireccion(createDto));
    }

    @Test
    void crearDireccion_conPersonaInexistente_lanzaRuntimeException() {
        DireccionCreateDto createDto = new DireccionCreateDto();
        createDto.setPersonaId(99L);

        when(personaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> direccionService.crearDireccion(createDto));
    }

    @Test
    void crearDireccion_comoPrincipal_desmarcaOtrasPrincipales() {
        DireccionCreateDto createDto = new DireccionCreateDto();
        createDto.setPersonaId(1L);
        createDto.setCalle("Nueva Calle");
        createDto.setEsPrincipal(true);

        Direccion otraDireccion = new Direccion();
        otraDireccion.setId(2L);
        otraDireccion.setEsPrincipal(true);

        when(personaRepository.findById(1L)).thenReturn(Optional.of(persona));
        when(direccionRepository.findByPersonaId(1L)).thenReturn(List.of(otraDireccion));
        when(direccionRepository.saveAll(anyList())).thenReturn(List.of(otraDireccion));
        when(direccionRepository.save(any(Direccion.class))).thenReturn(direccion);

        DireccionResponseDto resultado = direccionService.crearDireccion(createDto);

        assertNotNull(resultado);
        assertFalse(otraDireccion.getEsPrincipal());
        verify(direccionRepository).saveAll(anyList());
    }

    @Test
    void obtenerDireccionPorId_conIdExistente_retornaDireccionResponseDto() {
        when(direccionRepository.findById(1L)).thenReturn(Optional.of(direccion));

        DireccionResponseDto resultado = direccionService.obtenerDireccionPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerDireccionPorId_conIdInexistente_lanzaDireccionNotFoundException() {
        when(direccionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DireccionNotFoundException.class, () -> direccionService.obtenerDireccionPorId(99L));
    }

    @Test
    void obtenerDireccionesPorPersona_conPersonaExistente_retornaLista() {
        when(personaRepository.existsById(1L)).thenReturn(true);
        when(direccionRepository.findByPersonaId(1L)).thenReturn(List.of(direccion));

        List<DireccionListDto> resultado = direccionService.obtenerDireccionesPorPersona(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerDireccionesPorPersona_conPersonaInexistente_lanzaRuntimeException() {
        when(personaRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> direccionService.obtenerDireccionesPorPersona(99L));
    }

    @Test
    void obtenerDireccionPrincipalPorPersona_conDireccionPrincipal_retornaDireccionResponseDto() {
        when(personaRepository.existsById(1L)).thenReturn(true);
        when(direccionRepository.findByPersonaIdAndEsPrincipalTrue(1L)).thenReturn(Optional.of(direccion));

        DireccionResponseDto resultado = direccionService.obtenerDireccionPrincipalPorPersona(1L);

        assertNotNull(resultado);
        assertTrue(resultado.getEsPrincipal());
    }

    @Test
    void obtenerDireccionPrincipalPorPersona_sinDireccionPrincipal_lanzaDireccionNotFoundException() {
        when(personaRepository.existsById(1L)).thenReturn(true);
        when(direccionRepository.findByPersonaIdAndEsPrincipalTrue(1L)).thenReturn(Optional.empty());

        assertThrows(DireccionNotFoundException.class, () -> direccionService.obtenerDireccionPrincipalPorPersona(1L));
    }

    @Test
    void obtenerDirecciones_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Direccion> page = new PageImpl<>(List.of(direccion));

        when(direccionRepository.findAll(pageable)).thenReturn(page);

        Page<DireccionListDto> resultado = direccionService.obtenerDirecciones(pageable);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void buscarPorCiudad_retornaResultados() {
        when(direccionRepository.findByCiudadContainingIgnoreCase("Springfield")).thenReturn(List.of(direccion));

        List<DireccionListDto> resultado = direccionService.buscarPorCiudad("Springfield");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorProvincia_retornaResultados() {
        when(direccionRepository.findByProvinciaContainingIgnoreCase("Buenos Aires")).thenReturn(List.of(direccion));

        List<DireccionListDto> resultado = direccionService.buscarPorProvincia("Buenos Aires");

        assertEquals(1, resultado.size());
    }

    @Test
    void actualizarDireccion_conDatosValidos_retornaDireccionActualizada() {
        DireccionUpdateDto updateDto = new DireccionUpdateDto();
        updateDto.setCalle("Calle Nueva");

        when(direccionRepository.findById(1L)).thenReturn(Optional.of(direccion));
        when(direccionRepository.save(any(Direccion.class))).thenReturn(direccion);

        DireccionResponseDto resultado = direccionService.actualizarDireccion(1L, updateDto);

        assertNotNull(resultado);
        verify(direccionRepository).save(any(Direccion.class));
    }

    @Test
    void actualizarDireccion_conIdInexistente_lanzaDireccionNotFoundException() {
        DireccionUpdateDto updateDto = new DireccionUpdateDto();

        when(direccionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DireccionNotFoundException.class, () -> direccionService.actualizarDireccion(99L, updateDto));
    }

    @Test
    void actualizarDireccion_marcarComoPrincipal_desmarcaOtras() {
        direccion.setEsPrincipal(false);

        DireccionUpdateDto updateDto = new DireccionUpdateDto();
        updateDto.setEsPrincipal(true);

        when(direccionRepository.findById(1L)).thenReturn(Optional.of(direccion));
        when(direccionRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());
        when(direccionRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(direccionRepository.save(any(Direccion.class))).thenReturn(direccion);

        DireccionResponseDto resultado = direccionService.actualizarDireccion(1L, updateDto);

        assertNotNull(resultado);
        verify(direccionRepository).findByPersonaId(1L);
    }

    @Test
    void eliminarDireccion_conIdExistente_eliminaDireccion() {
        when(direccionRepository.existsById(1L)).thenReturn(true);

        direccionService.eliminarDireccion(1L);

        verify(direccionRepository).deleteById(1L);
    }

    @Test
    void eliminarDireccion_conIdInexistente_lanzaDireccionNotFoundException() {
        when(direccionRepository.existsById(99L)).thenReturn(false);

        assertThrows(DireccionNotFoundException.class, () -> direccionService.eliminarDireccion(99L));
    }

    @Test
    void marcarComoPrincipal_conIdExistente_marcaDireccion() {
        direccion.setEsPrincipal(false);

        when(direccionRepository.findById(1L)).thenReturn(Optional.of(direccion));
        when(direccionRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());
        when(direccionRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(direccionRepository.save(any(Direccion.class))).thenReturn(direccion);

        DireccionResponseDto resultado = direccionService.marcarComoPrincipal(1L);

        assertNotNull(resultado);
        assertTrue(direccion.getEsPrincipal());
    }

    @Test
    void marcarComoPrincipal_conIdInexistente_lanzaDireccionNotFoundException() {
        when(direccionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DireccionNotFoundException.class, () -> direccionService.marcarComoPrincipal(99L));
    }
}
