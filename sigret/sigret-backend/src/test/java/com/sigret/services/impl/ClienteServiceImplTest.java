package com.sigret.services.impl;

import com.sigret.dtos.cliente.ClienteCreateDto;
import com.sigret.dtos.cliente.ClienteListDto;
import com.sigret.dtos.cliente.ClienteResponseDto;
import com.sigret.dtos.cliente.ClienteUpdateDto;
import com.sigret.dtos.contacto.ContactoCreateDto;
import com.sigret.dtos.direccion.DireccionCreateDto;
import com.sigret.entities.*;
import com.sigret.exception.ClienteNotFoundException;
import com.sigret.exception.DocumentoAlreadyExistsException;
import com.sigret.exception.TipoDocumentoNotFoundException;
import com.sigret.exception.TipoPersonaNotFoundException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private DireccionRepository direccionRepository;

    @Mock
    private ContactoRepository contactoRepository;

    @Mock
    private TipoPersonaRepository tipoPersonaRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Mock
    private TipoContactoRepository tipoContactoRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private TipoPersona tipoPersona;
    private TipoDocumento tipoDocumento;
    private TipoContacto tipoContacto;
    private Persona persona;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        tipoPersona = new TipoPersona();
        tipoPersona.setId(1L);
        tipoPersona.setDescripcion("Física");

        tipoDocumento = new TipoDocumento();
        tipoDocumento.setId(1L);
        tipoDocumento.setDescripcion("DNI");

        tipoContacto = new TipoContacto();
        tipoContacto.setId(1L);
        tipoContacto.setDescripcion("Email");

        persona = new Persona();
        persona.setId(1L);
        persona.setTipoPersona(tipoPersona);
        persona.setNombre("Juan");
        persona.setApellido("Pérez");
        persona.setTipoDocumento(tipoDocumento);
        persona.setDocumento("12345678");
        persona.setSexo("M");
        persona.setContactos(new ArrayList<>());
        persona.setDirecciones(new ArrayList<>());

        cliente = new Cliente(persona);
        cliente.setId(1L);
        cliente.setComentarios("Cliente frecuente");
        cliente.setActivo(true);
    }

    @Test
    void crearCliente_conDatosValidos_retornaClienteResponseDto() {
        ClienteCreateDto createDto = new ClienteCreateDto();
        createDto.setTipoPersonaId(1L);
        createDto.setTipoDocumentoId(1L);
        createDto.setNombre("Juan");
        createDto.setApellido("Pérez");
        createDto.setDocumento("12345678");
        createDto.setSexo("M");

        when(clienteRepository.existsByPersonaDocumento("12345678")).thenReturn(false);
        when(tipoPersonaRepository.findById(1L)).thenReturn(Optional.of(tipoPersona));
        when(tipoDocumentoRepository.findById(1L)).thenReturn(Optional.of(tipoDocumento));
        when(personaRepository.save(any(Persona.class))).thenReturn(persona);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(contactoRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());
        when(direccionRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());

        ClienteResponseDto resultado = clienteService.crearCliente(createDto);

        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombreCompleto());
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void crearCliente_conDocumentoDuplicado_lanzaDocumentoAlreadyExistsException() {
        ClienteCreateDto createDto = new ClienteCreateDto();
        createDto.setDocumento("12345678");

        when(clienteRepository.existsByPersonaDocumento("12345678")).thenReturn(true);

        assertThrows(DocumentoAlreadyExistsException.class, () -> clienteService.crearCliente(createDto));
    }

    @Test
    void crearCliente_conTipoPersonaInexistente_lanzaTipoPersonaNotFoundException() {
        ClienteCreateDto createDto = new ClienteCreateDto();
        createDto.setDocumento("99999999");
        createDto.setTipoPersonaId(99L);

        when(clienteRepository.existsByPersonaDocumento("99999999")).thenReturn(false);
        when(tipoPersonaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TipoPersonaNotFoundException.class, () -> clienteService.crearCliente(createDto));
    }

    @Test
    void crearCliente_conTipoDocumentoInexistente_lanzaTipoDocumentoNotFoundException() {
        ClienteCreateDto createDto = new ClienteCreateDto();
        createDto.setDocumento("99999999");
        createDto.setTipoPersonaId(1L);
        createDto.setTipoDocumentoId(99L);

        when(clienteRepository.existsByPersonaDocumento("99999999")).thenReturn(false);
        when(tipoPersonaRepository.findById(1L)).thenReturn(Optional.of(tipoPersona));
        when(tipoDocumentoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TipoDocumentoNotFoundException.class, () -> clienteService.crearCliente(createDto));
    }

    @Test
    void crearCliente_conContactos_creaContactos() {
        ContactoCreateDto contactoDto = new ContactoCreateDto();
        contactoDto.setTipoContactoId(1L);
        contactoDto.setDescripcion("juan@mail.com");

        ClienteCreateDto createDto = new ClienteCreateDto();
        createDto.setTipoPersonaId(1L);
        createDto.setTipoDocumentoId(1L);
        createDto.setNombre("Juan");
        createDto.setApellido("Pérez");
        createDto.setDocumento("12345678");
        createDto.setContactos(List.of(contactoDto));

        when(clienteRepository.existsByPersonaDocumento("12345678")).thenReturn(false);
        when(tipoPersonaRepository.findById(1L)).thenReturn(Optional.of(tipoPersona));
        when(tipoDocumentoRepository.findById(1L)).thenReturn(Optional.of(tipoDocumento));
        when(personaRepository.save(any(Persona.class))).thenReturn(persona);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(tipoContactoRepository.findById(1L)).thenReturn(Optional.of(tipoContacto));
        when(contactoRepository.save(any(Contacto.class))).thenReturn(new Contacto());
        when(contactoRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());
        when(direccionRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());

        ClienteResponseDto resultado = clienteService.crearCliente(createDto);

        assertNotNull(resultado);
        verify(contactoRepository).save(any(Contacto.class));
    }

    @Test
    void obtenerClientePorId_conIdExistente_retornaClienteResponseDto() {
        when(clienteRepository.findByIdWithPersona(1L)).thenReturn(Optional.of(cliente));
        when(contactoRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());
        when(direccionRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());

        ClienteResponseDto resultado = clienteService.obtenerClientePorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerClientePorId_conIdInexistente_lanzaClienteNotFoundException() {
        when(clienteRepository.findByIdWithPersona(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.obtenerClientePorId(99L));
    }

    @Test
    void obtenerClientes_conFiltro_retornaPaginaFiltrada() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> page = new PageImpl<>(List.of(cliente));

        when(clienteRepository.buscarClientesConFiltros("Juan", pageable)).thenReturn(page);
        when(direccionRepository.findByPersonaId(anyLong())).thenReturn(new ArrayList<>());

        Page<ClienteListDto> resultado = clienteService.obtenerClientes(pageable, "Juan");

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void obtenerClientes_sinFiltro_retornaTodosLosActivos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> page = new PageImpl<>(List.of(cliente));

        when(clienteRepository.findByActivoTrue(pageable)).thenReturn(page);
        when(direccionRepository.findByPersonaId(anyLong())).thenReturn(new ArrayList<>());

        Page<ClienteListDto> resultado = clienteService.obtenerClientes(pageable, null);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void obtenerTodosLosClientes_retornaLista() {
        when(clienteRepository.findByActivoTrue()).thenReturn(List.of(cliente));
        when(direccionRepository.findByPersonaId(anyLong())).thenReturn(new ArrayList<>());

        List<ClienteListDto> resultado = clienteService.obtenerTodosLosClientes();

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarClientesAutocompletado_conTermino_retornaResultados() {
        when(clienteRepository.buscarClientesPorTermino(eq("Juan"), any(Pageable.class))).thenReturn(List.of(cliente));
        when(direccionRepository.findByPersonaId(anyLong())).thenReturn(new ArrayList<>());

        List<ClienteListDto> resultado = clienteService.buscarClientesAutocompletado("Juan", 5);

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarClientesAutocompletado_conTerminoVacio_retornaListaVacia() {
        List<ClienteListDto> resultado = clienteService.buscarClientesAutocompletado("", 5);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarClientesAutocompletado_conTerminoNull_retornaListaVacia() {
        List<ClienteListDto> resultado = clienteService.buscarClientesAutocompletado(null, 5);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void obtenerClientePorDocumento_conDocumentoExistente_retornaClienteResponseDto() {
        when(clienteRepository.findByPersonaDocumentoAndActivoTrue("12345678")).thenReturn(Optional.of(cliente));
        when(contactoRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());
        when(direccionRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());

        ClienteResponseDto resultado = clienteService.obtenerClientePorDocumento("12345678");

        assertNotNull(resultado);
    }

    @Test
    void obtenerClientePorDocumento_conDocumentoInexistente_lanzaClienteNotFoundException() {
        when(clienteRepository.findByPersonaDocumentoAndActivoTrue("00000000")).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.obtenerClientePorDocumento("00000000"));
    }

    @Test
    void actualizarCliente_conDatosValidos_retornaClienteActualizado() {
        ClienteUpdateDto updateDto = new ClienteUpdateDto();
        updateDto.setNombre("Juan Carlos");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(personaRepository.save(any(Persona.class))).thenReturn(persona);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(contactoRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());
        when(direccionRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());

        ClienteResponseDto resultado = clienteService.actualizarCliente(1L, updateDto);

        assertNotNull(resultado);
        verify(personaRepository).save(any(Persona.class));
    }

    @Test
    void actualizarCliente_conIdInexistente_lanzaClienteNotFoundException() {
        ClienteUpdateDto updateDto = new ClienteUpdateDto();

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.actualizarCliente(99L, updateDto));
    }

    @Test
    void eliminarCliente_conIdExistente_desactivaCliente() {
        when(clienteRepository.findByIdIncludingInactive(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        clienteService.eliminarCliente(1L);

        assertFalse(cliente.getActivo());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void eliminarCliente_conIdInexistente_lanzaClienteNotFoundException() {
        when(clienteRepository.findByIdIncludingInactive(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.eliminarCliente(99L));
    }

    @Test
    void reactivarCliente_conIdExistente_activaCliente() {
        cliente.setActivo(false);
        when(clienteRepository.findByIdIncludingInactive(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        clienteService.reactivarCliente(1L);

        assertTrue(cliente.getActivo());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void reactivarCliente_conIdInexistente_lanzaClienteNotFoundException() {
        when(clienteRepository.findByIdIncludingInactive(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.reactivarCliente(99L));
    }

    @Test
    void obtenerClienteConEquipos_conIdExistente_retornaClienteResponseDto() {
        when(clienteRepository.findByIdWithPersona(1L)).thenReturn(Optional.of(cliente));
        when(contactoRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());
        when(direccionRepository.findByPersonaId(1L)).thenReturn(new ArrayList<>());

        ClienteResponseDto resultado = clienteService.obtenerClienteConEquipos(1L);

        assertNotNull(resultado);
    }

    @Test
    void obtenerClienteConEquipos_conIdInexistente_lanzaClienteNotFoundException() {
        when(clienteRepository.findByIdWithPersona(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.obtenerClienteConEquipos(99L));
    }
}
