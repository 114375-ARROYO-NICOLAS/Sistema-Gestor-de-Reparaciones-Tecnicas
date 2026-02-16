package com.sigret.services.impl;

import com.sigret.dtos.presupuesto.*;
import com.sigret.entities.*;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.enums.EstadoServicio;
import com.sigret.exception.PresupuestoNotFoundException;
import com.sigret.repositories.*;
import com.sigret.services.EmailService;
import com.sigret.services.PresupuestoTokenService;
import com.sigret.services.WebSocketNotificationService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresupuestoServiceImplTest {

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private OrdenTrabajoRepository ordenTrabajoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private WebSocketNotificationService notificationService;

    @Mock
    private EmailService emailService;

    @Mock
    private PresupuestoTokenService tokenService;

    @InjectMocks
    private PresupuestoServiceImpl presupuestoService;

    private Servicio servicio;
    private Empleado empleado;
    private Presupuesto presupuesto;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        Persona personaCliente = new Persona();
        personaCliente.setId(1L);
        personaCliente.setNombre("Juan");
        personaCliente.setApellido("Perez");

        TipoEquipo tipoEquipo = new TipoEquipo();
        tipoEquipo.setId(1L);
        tipoEquipo.setDescripcion("Notebook");

        Marca marca = new Marca();
        marca.setId(1L);
        marca.setDescripcion("Samsung");

        Equipo equipo = new Equipo();
        equipo.setId(1L);
        equipo.setTipoEquipo(tipoEquipo);
        equipo.setMarca(marca);

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setPersona(personaCliente);
        cliente.setActivo(true);

        Persona personaEmpleado = new Persona();
        personaEmpleado.setId(2L);
        personaEmpleado.setNombre("Carlos");
        personaEmpleado.setApellido("Tecnico");

        empleado = new Empleado();
        empleado.setId(1L);
        empleado.setPersona(personaEmpleado);

        servicio = new Servicio();
        servicio.setId(1L);
        servicio.setNumeroServicio("SRV2600001");
        servicio.setCliente(cliente);
        servicio.setEquipo(equipo);
        servicio.setEstado(EstadoServicio.RECIBIDO);
        servicio.setEsGarantia(false);

        presupuesto = new Presupuesto();
        presupuesto.setId(1L);
        presupuesto.setNumeroPresupuesto("PRE2600001");
        presupuesto.setServicio(servicio);
        presupuesto.setEmpleado(empleado);
        presupuesto.setDiagnostico("Pantalla rota");
        presupuesto.setManoObra(new BigDecimal("5000"));
        presupuesto.setMontoRepuestosOriginal(new BigDecimal("10000"));
        presupuesto.setMontoTotalOriginal(new BigDecimal("15000"));
        presupuesto.setMostrarOriginal(true);
        presupuesto.setMostrarAlternativo(false);
        presupuesto.setEstado(EstadoPresupuesto.PENDIENTE);
        presupuesto.setFechaCreacion(LocalDateTime.now());
        presupuesto.setDetallePresupuestos(new ArrayList<>());
        presupuesto.setOrdenesTrabajo(new ArrayList<>());
    }

    @Test
    void crearPresupuesto_conDatosValidos_retornaPresupuestoResponseDto() {
        PresupuestoCreateDto createDto = new PresupuestoCreateDto();
        createDto.setServicioId(1L);
        createDto.setEmpleadoId(1L);
        createDto.setDiagnostico("Pantalla rota");
        createDto.setManoObra(new BigDecimal("5000"));
        createDto.setMontoRepuestosOriginal(new BigDecimal("10000"));
        createDto.setMostrarOriginal(true);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(presupuestoRepository.findMaxNumeroPresupuesto(any())).thenReturn(null);
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuesto);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        PresupuestoResponseDto resultado = presupuestoService.crearPresupuesto(createDto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(notificationService).notificarPresupuesto(any(PresupuestoEventDto.class));
    }

    @Test
    void crearPresupuesto_conServicioInexistente_lanzaRuntimeException() {
        PresupuestoCreateDto createDto = new PresupuestoCreateDto();
        createDto.setServicioId(99L);
        createDto.setEmpleadoId(1L);

        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> presupuestoService.crearPresupuesto(createDto));
    }

    @Test
    void crearPresupuesto_conDetalles_calculaMontosCorrectamente() {
        DetallePresupuestoDto detalleDto = new DetallePresupuestoDto();
        detalleDto.setItem("Pantalla LCD");
        detalleDto.setCantidad(1);
        detalleDto.setPrecioOriginal(new BigDecimal("10000"));
        detalleDto.setPrecioAlternativo(new BigDecimal("7000"));

        PresupuestoCreateDto createDto = new PresupuestoCreateDto();
        createDto.setServicioId(1L);
        createDto.setEmpleadoId(1L);
        createDto.setManoObra(new BigDecimal("5000"));
        createDto.setDetalles(List.of(detalleDto));
        createDto.setMostrarOriginal(true);
        createDto.setMostrarAlternativo(true);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(presupuestoRepository.findMaxNumeroPresupuesto(any())).thenReturn(null);
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuesto);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        PresupuestoResponseDto resultado = presupuestoService.crearPresupuesto(createDto);

        assertNotNull(resultado);
        verify(presupuestoRepository).save(any(Presupuesto.class));
    }

    @Test
    void obtenerPresupuestoPorId_conIdExistente_retornaPresupuestoResponseDto() {
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));

        PresupuestoResponseDto resultado = presupuestoService.obtenerPresupuestoPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("PRE2600001", resultado.getNumeroPresupuesto());
    }

    @Test
    void obtenerPresupuestoPorId_conIdInexistente_lanzaPresupuestoNotFoundException() {
        when(presupuestoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PresupuestoNotFoundException.class, () -> presupuestoService.obtenerPresupuestoPorId(99L));
    }

    @Test
    void obtenerPresupuestos_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Presupuesto> page = new PageImpl<>(List.of(presupuesto));

        when(presupuestoRepository.findByEstadoAndFechaVencimientoBefore(eq(EstadoPresupuesto.ENVIADO), any(LocalDate.class)))
                .thenReturn(List.of());
        when(presupuestoRepository.findAll(pageable)).thenReturn(page);

        Page<PresupuestoListDto> resultado = presupuestoService.obtenerPresupuestos(pageable);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void obtenerPresupuestosPorServicio_retornaLista() {
        when(presupuestoRepository.findByServicioId(1L)).thenReturn(List.of(presupuesto));

        List<PresupuestoListDto> resultado = presupuestoService.obtenerPresupuestosPorServicio(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerPresupuestosPorEstado_retornaLista() {
        when(presupuestoRepository.findByEstado(EstadoPresupuesto.PENDIENTE)).thenReturn(List.of(presupuesto));

        List<PresupuestoListDto> resultado = presupuestoService.obtenerPresupuestosPorEstado(EstadoPresupuesto.PENDIENTE);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerPresupuestosPorCliente_retornaLista() {
        when(presupuestoRepository.findByClienteId(1L)).thenReturn(List.of(presupuesto));

        List<PresupuestoListDto> resultado = presupuestoService.obtenerPresupuestosPorCliente(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void actualizarPresupuesto_conDatosValidos_retornaPresupuestoActualizado() {
        PresupuestoUpdateDto updateDto = new PresupuestoUpdateDto();
        updateDto.setDiagnostico("Pantalla y teclado rotos");
        updateDto.setManoObra(new BigDecimal("7000"));

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuesto);

        PresupuestoResponseDto resultado = presupuestoService.actualizarPresupuesto(1L, updateDto);

        assertNotNull(resultado);
        verify(presupuestoRepository).save(any(Presupuesto.class));
    }

    @Test
    void cambiarEstadoPresupuesto_aListo_actualizaEstadoServicio() {
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuesto);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        presupuestoService.cambiarEstadoPresupuesto(1L, EstadoPresupuesto.LISTO);

        verify(servicioRepository).save(any(Servicio.class));
        verify(notificationService).notificarPresupuesto(any(PresupuestoEventDto.class));
    }

    @Test
    void aprobarPresupuesto_sinTipoPrecio_retornaPresupuestoAprobado() {
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuesto);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        PresupuestoResponseDto resultado = presupuestoService.aprobarPresupuesto(1L);

        assertNotNull(resultado);
        verify(presupuestoRepository).save(any(Presupuesto.class));
        verify(servicioRepository).save(any(Servicio.class));
    }

    @Test
    void aprobarPresupuesto_conTipoPrecioOriginal_guardaTipoConfirmacion() {
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuesto);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        presupuestoService.aprobarPresupuesto(1L, "ORIGINAL");

        assertEquals(EstadoPresupuesto.APROBADO, presupuesto.getEstado());
        verify(notificationService).notificarPresupuesto(any(PresupuestoEventDto.class));
    }

    @Test
    void aprobarPresupuesto_conTipoPrecioAlternativo_guardaTipoConfirmacion() {
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuesto);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        presupuestoService.aprobarPresupuesto(1L, "ALTERNATIVO");

        assertEquals(EstadoPresupuesto.APROBADO, presupuesto.getEstado());
    }

    @Test
    void rechazarPresupuesto_retornaPresupuestoRechazado() {
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuesto);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        PresupuestoResponseDto resultado = presupuestoService.rechazarPresupuesto(1L);

        assertNotNull(resultado);
        assertEquals(EstadoPresupuesto.RECHAZADO, presupuesto.getEstado());
        assertEquals(EstadoServicio.RECHAZADO, servicio.getEstado());
    }

    @Test
    void asignarEmpleado_conDatosValidos_retornaPresupuestoConEmpleado() {
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuesto);

        PresupuestoResponseDto resultado = presupuestoService.asignarEmpleado(1L, 1L);

        assertNotNull(resultado);
        verify(notificationService).notificarPresupuesto(any(PresupuestoEventDto.class));
    }

    @Test
    void crearOrdenDeTrabajo_conPresupuestoAprobado_retornaOrdenId() {
        presupuesto.setEstado(EstadoPresupuesto.APROBADO);
        presupuesto.setOrdenesTrabajo(new ArrayList<>());

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(ordenTrabajoRepository.findMaxNumeroOrdenTrabajo(any())).thenReturn(null);

        OrdenTrabajo ordenTrabajo = new OrdenTrabajo();
        ordenTrabajo.setId(1L);
        ordenTrabajo.setDetalleOrdenesTrabajo(new ArrayList<>());
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenTrabajo);

        Long ordenId = presupuestoService.crearOrdenDeTrabajo(1L);

        assertNotNull(ordenId);
        assertEquals(1L, ordenId);
    }

    @Test
    void crearOrdenDeTrabajo_conPresupuestoNoAprobado_lanzaRuntimeException() {
        presupuesto.setEstado(EstadoPresupuesto.PENDIENTE);

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));

        assertThrows(RuntimeException.class, () -> presupuestoService.crearOrdenDeTrabajo(1L));
    }

    @Test
    void crearOrdenDeTrabajo_conOrdenExistente_lanzaRuntimeException() {
        presupuesto.setEstado(EstadoPresupuesto.APROBADO);
        presupuesto.setOrdenesTrabajo(List.of(new OrdenTrabajo()));

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));

        assertThrows(RuntimeException.class, () -> presupuestoService.crearOrdenDeTrabajo(1L));
    }

    @Test
    void eliminarPresupuesto_conIdExistente_eliminaPresupuesto() {
        when(presupuestoRepository.existsById(1L)).thenReturn(true);

        presupuestoService.eliminarPresupuesto(1L);

        verify(presupuestoRepository).deleteById(1L);
    }

    @Test
    void eliminarPresupuesto_conIdInexistente_lanzaPresupuestoNotFoundException() {
        when(presupuestoRepository.existsById(99L)).thenReturn(false);

        assertThrows(PresupuestoNotFoundException.class, () -> presupuestoService.eliminarPresupuesto(99L));
    }

    @Test
    void generarNumeroPresupuesto_sinPresupuestosPrevios_retornaPrimerNumero() {
        when(presupuestoRepository.findMaxNumeroPresupuesto(any())).thenReturn(null);

        String numero = presupuestoService.generarNumeroPresupuesto();

        assertNotNull(numero);
        assertTrue(numero.startsWith("PRE"));
        assertTrue(numero.endsWith("00001"));
    }

    @Test
    void generarNumeroPresupuesto_conPresupuestosPrevios_retornaSiguienteNumero() {
        when(presupuestoRepository.findMaxNumeroPresupuesto(any())).thenReturn(5);

        String numero = presupuestoService.generarNumeroPresupuesto();

        assertNotNull(numero);
        assertTrue(numero.startsWith("PRE"));
        assertTrue(numero.endsWith("00006"));
    }

    @Test
    void actualizarYReenviar_conEstadoEnviado_actualizaYReenvia() {
        presupuesto.setEstado(EstadoPresupuesto.ENVIADO);

        PresupuestoActualizarReenviarDto dto = new PresupuestoActualizarReenviarDto();
        dto.setFechaVencimiento(LocalDate.now().plusDays(7));
        dto.setManoObra(new BigDecimal("6000"));
        dto.setReenviarEmail(true);
        dto.setMostrarOriginal(true);
        dto.setMostrarAlternativo(false);
        dto.setMensajeAdicional("Mensaje de prueba");

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(presupuesto);

        PresupuestoResponseDto resultado = presupuestoService.actualizarYReenviar(1L, dto);

        assertNotNull(resultado);
        verify(tokenService).invalidarTokensAnteriores(1L);
        verify(emailService).enviarPresupuestoACliente(eq(1L), eq(true), eq(false), eq("Mensaje de prueba"));
    }

    @Test
    void actualizarYReenviar_conEstadoPendiente_lanzaRuntimeException() {
        presupuesto.setEstado(EstadoPresupuesto.PENDIENTE);

        PresupuestoActualizarReenviarDto dto = new PresupuestoActualizarReenviarDto();
        dto.setFechaVencimiento(LocalDate.now().plusDays(7));

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));

        assertThrows(RuntimeException.class, () -> presupuestoService.actualizarYReenviar(1L, dto));
    }

    @Test
    void actualizarYReenviar_conFechaVencimientoPasada_lanzaRuntimeException() {
        presupuesto.setEstado(EstadoPresupuesto.ENVIADO);

        PresupuestoActualizarReenviarDto dto = new PresupuestoActualizarReenviarDto();
        dto.setFechaVencimiento(LocalDate.now().minusDays(1));

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));

        assertThrows(RuntimeException.class, () -> presupuestoService.actualizarYReenviar(1L, dto));
    }
}
