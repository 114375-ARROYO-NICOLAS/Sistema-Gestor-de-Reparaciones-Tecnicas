package com.sigret.services.impl;

import com.sigret.dtos.equipo.EquipoCreateDto;
import com.sigret.dtos.equipo.EquipoListDto;
import com.sigret.dtos.equipo.EquipoResponseDto;
import com.sigret.dtos.equipo.EquipoUpdateDto;
import com.sigret.entities.*;
import com.sigret.exception.ClienteNotFoundException;
import com.sigret.exception.EquipoNotFoundException;
import com.sigret.exception.NumeroSerieAlreadyExistsException;
import com.sigret.repositories.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipoServiceImplTest {

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @Mock
    private ModeloRepository modeloRepository;

    @Mock
    private TipoEquipoRepository tipoEquipoRepository;

    @Mock
    private ClienteEquipoRepository clienteEquipoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private EquipoServiceImpl equipoService;

    private TipoEquipo tipoEquipo;
    private Marca marca;
    private Modelo modelo;
    private Equipo equipo;

    @BeforeEach
    void setUp() {
        tipoEquipo = new TipoEquipo();
        tipoEquipo.setId(1L);
        tipoEquipo.setDescripcion("Notebook");

        marca = new Marca();
        marca.setId(1L);
        marca.setDescripcion("Samsung");

        modelo = new Modelo();
        modelo.setId(1L);
        modelo.setDescripcion("Galaxy Book");
        modelo.setMarca(marca);

        equipo = new Equipo();
        equipo.setId(1L);
        equipo.setTipoEquipo(tipoEquipo);
        equipo.setMarca(marca);
        equipo.setModelo(modelo);
        equipo.setNumeroSerie("SN12345");
        equipo.setColor("Negro");
        equipo.setObservaciones("Sin rayaduras");
        equipo.setClienteEquipos(new ArrayList<>());
    }

    @Test
    void crearEquipo_conDatosValidos_retornaEquipoResponseDto() {
        EquipoCreateDto createDto = new EquipoCreateDto();
        createDto.setTipoEquipoId(1L);
        createDto.setMarcaId(1L);
        createDto.setModeloId(1L);
        createDto.setNumeroSerie("SN12345");
        createDto.setColor("Negro");

        when(equipoRepository.existsByNumeroSerie("SN12345")).thenReturn(false);
        when(tipoEquipoRepository.findById(1L)).thenReturn(Optional.of(tipoEquipo));
        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));
        when(modeloRepository.findById(1L)).thenReturn(Optional.of(modelo));
        when(equipoRepository.save(any(Equipo.class))).thenReturn(equipo);

        EquipoResponseDto resultado = equipoService.crearEquipo(createDto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("SN12345", resultado.getNumeroSerie());
    }

    @Test
    void crearEquipo_conNumeroSerieDuplicado_lanzaNumeroSerieAlreadyExistsException() {
        EquipoCreateDto createDto = new EquipoCreateDto();
        createDto.setNumeroSerie("SN12345");
        createDto.setTipoEquipoId(1L);
        createDto.setMarcaId(1L);

        when(equipoRepository.existsByNumeroSerie("SN12345")).thenReturn(true);

        assertThrows(NumeroSerieAlreadyExistsException.class, () -> equipoService.crearEquipo(createDto));
    }

    @Test
    void crearEquipo_sinModelo_retornaEquipoResponseDto() {
        EquipoCreateDto createDto = new EquipoCreateDto();
        createDto.setTipoEquipoId(1L);
        createDto.setMarcaId(1L);
        createDto.setModeloId(null);
        createDto.setNumeroSerie("SN12345");

        Equipo equipoSinModelo = new Equipo();
        equipoSinModelo.setId(2L);
        equipoSinModelo.setTipoEquipo(tipoEquipo);
        equipoSinModelo.setMarca(marca);
        equipoSinModelo.setModelo(null);
        equipoSinModelo.setNumeroSerie("SN12345");
        equipoSinModelo.setClienteEquipos(new ArrayList<>());

        when(equipoRepository.existsByNumeroSerie("SN12345")).thenReturn(false);
        when(tipoEquipoRepository.findById(1L)).thenReturn(Optional.of(tipoEquipo));
        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));
        when(equipoRepository.save(any(Equipo.class))).thenReturn(equipoSinModelo);

        EquipoResponseDto resultado = equipoService.crearEquipo(createDto);

        assertNotNull(resultado);
        assertNull(resultado.getModelo());
    }

    @Test
    void obtenerEquipoPorId_conIdExistente_retornaEquipoResponseDto() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));

        EquipoResponseDto resultado = equipoService.obtenerEquipoPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerEquipoPorId_conIdInexistente_lanzaEquipoNotFoundException() {
        when(equipoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EquipoNotFoundException.class, () -> equipoService.obtenerEquipoPorId(99L));
    }

    @Test
    void obtenerEquipos_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Equipo> page = new PageImpl<>(List.of(equipo));
        when(equipoRepository.findAll(pageable)).thenReturn(page);

        Page<EquipoListDto> resultado = equipoService.obtenerEquipos(pageable);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void obtenerTodosLosEquipos_retornaLista() {
        when(equipoRepository.findAll()).thenReturn(List.of(equipo));

        List<EquipoListDto> resultado = equipoService.obtenerTodosLosEquipos();

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerEquiposPorMarca_retornaListaFiltrada() {
        when(equipoRepository.findByMarcaId(1L)).thenReturn(List.of(equipo));

        List<EquipoListDto> resultado = equipoService.obtenerEquiposPorMarca(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerEquiposPorTipo_retornaListaFiltrada() {
        when(equipoRepository.findByTipoEquipoId(1L)).thenReturn(List.of(equipo));

        List<EquipoListDto> resultado = equipoService.obtenerEquiposPorTipo(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void actualizarEquipo_conDatosValidos_retornaEquipoActualizado() {
        EquipoUpdateDto updateDto = new EquipoUpdateDto();
        updateDto.setColor("Blanco");

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(equipoRepository.save(any(Equipo.class))).thenReturn(equipo);

        EquipoResponseDto resultado = equipoService.actualizarEquipo(1L, updateDto);

        assertNotNull(resultado);
        verify(equipoRepository).save(any(Equipo.class));
    }

    @Test
    void actualizarEquipo_conIdInexistente_lanzaEquipoNotFoundException() {
        EquipoUpdateDto updateDto = new EquipoUpdateDto();
        when(equipoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EquipoNotFoundException.class, () -> equipoService.actualizarEquipo(99L, updateDto));
    }

    @Test
    void eliminarEquipo_conIdExistente_eliminaEquipo() {
        when(equipoRepository.existsById(1L)).thenReturn(true);

        equipoService.eliminarEquipo(1L);

        verify(equipoRepository).deleteById(1L);
    }

    @Test
    void eliminarEquipo_conIdInexistente_lanzaEquipoNotFoundException() {
        when(equipoRepository.existsById(99L)).thenReturn(false);

        assertThrows(EquipoNotFoundException.class, () -> equipoService.eliminarEquipo(99L));
    }

    @Test
    void existeEquipoConNumeroSerie_retornaTrue() {
        when(equipoRepository.existsByNumeroSerie("SN12345")).thenReturn(true);

        assertTrue(equipoService.existeEquipoConNumeroSerie("SN12345"));
    }

    @Test
    void obtenerEquiposPorCliente_retornaListaDeEquipos() {
        when(clienteEquipoRepository.findEquiposByClienteId(1L)).thenReturn(List.of(equipo));

        List<EquipoListDto> resultado = equipoService.obtenerEquiposPorCliente(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void asociarEquipoACliente_conDatosValidos_creaAsociacion() {
        Persona persona = new Persona();
        persona.setId(1L);
        persona.setNombre("Juan");
        persona.setApellido("Perez");

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setPersona(persona);

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteEquipoRepository.existsByClienteIdAndEquipoId(1L, 1L)).thenReturn(false);
        when(clienteEquipoRepository.save(any(ClienteEquipo.class))).thenReturn(new ClienteEquipo());

        equipoService.asociarEquipoACliente(1L, 1L);

        verify(clienteEquipoRepository).save(any(ClienteEquipo.class));
    }

    @Test
    void asociarEquipoACliente_yaAsociado_lanzaIllegalStateException() {
        Persona persona = new Persona();
        persona.setId(1L);
        persona.setNombre("Juan");

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setPersona(persona);

        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteEquipoRepository.existsByClienteIdAndEquipoId(1L, 1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> equipoService.asociarEquipoACliente(1L, 1L));
    }

    @Test
    void desasociarEquipoDeCliente_conAsociacionActiva_desasocia() {
        ClienteEquipo clienteEquipo = new ClienteEquipo();
        clienteEquipo.setId(1L);
        clienteEquipo.setActivo(true);

        when(clienteEquipoRepository.findByClienteIdAndEquipoIdAndActivoTrue(1L, 1L)).thenReturn(clienteEquipo);
        when(clienteEquipoRepository.save(any(ClienteEquipo.class))).thenReturn(clienteEquipo);

        equipoService.desasociarEquipoDeCliente(1L, 1L);

        assertFalse(clienteEquipo.getActivo());
        verify(clienteEquipoRepository).save(clienteEquipo);
    }

    @Test
    void desasociarEquipoDeCliente_sinAsociacion_lanzaIllegalStateException() {
        when(clienteEquipoRepository.findByClienteIdAndEquipoIdAndActivoTrue(1L, 1L)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> equipoService.desasociarEquipoDeCliente(1L, 1L));
    }
}
