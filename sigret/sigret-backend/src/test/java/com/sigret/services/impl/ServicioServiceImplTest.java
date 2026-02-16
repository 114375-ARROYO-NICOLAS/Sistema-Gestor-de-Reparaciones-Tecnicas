package com.sigret.services.impl;

import com.sigret.dtos.servicio.*;
import com.sigret.entities.*;
import com.sigret.enums.EstadoOrdenTrabajo;
import com.sigret.enums.EstadoServicio;
import com.sigret.enums.TipoIngreso;
import com.sigret.exception.ServicioNotFoundException;
import com.sigret.repositories.*;
import com.sigret.services.PresupuestoService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceImplTest {

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private RepuestoRepository repuestoRepository;

    @Mock
    private WebSocketNotificationService notificationService;

    @Mock
    private PresupuestoService presupuestoService;

    @InjectMocks
    private ServicioServiceImpl servicioService;

    private Cliente cliente;
    private Equipo equipo;
    private Empleado empleado;
    private Servicio servicio;

    @BeforeEach
    void setUp() {
        Persona personaCliente = new Persona();
        personaCliente.setId(1L);
        personaCliente.setNombre("Juan");
        personaCliente.setApellido("Perez");
        personaCliente.setContactos(new ArrayList<>());

        TipoEquipo tipoEquipo = new TipoEquipo();
        tipoEquipo.setId(1L);
        tipoEquipo.setDescripcion("Notebook");

        Marca marca = new Marca();
        marca.setId(1L);
        marca.setDescripcion("Samsung");

        equipo = new Equipo();
        equipo.setId(1L);
        equipo.setTipoEquipo(tipoEquipo);
        equipo.setMarca(marca);
        equipo.setNumeroSerie("SN123");

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setPersona(personaCliente);
        cliente.setActivo(true);
        cliente.setClienteEquipos(new ArrayList<>());

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
        servicio.setEmpleadoRecepcion(empleado);
        servicio.setTipoIngreso(TipoIngreso.CLIENTE_TRAE);
        servicio.setFallaReportada("No enciende");
        servicio.setEstado(EstadoServicio.RECIBIDO);
        servicio.setEsGarantia(false);
        servicio.setAbonaVisita(false);
        servicio.setMontoVisita(BigDecimal.ZERO);
        servicio.setFechaCreacion(LocalDateTime.now());
        servicio.setFechaRecepcion(LocalDate.now());
        servicio.setActivo(true);
        servicio.setDetalleServicios(new ArrayList<>());
        servicio.setOrdenesTrabajo(new ArrayList<>());
        servicio.setPresupuestos(new ArrayList<>());
    }

    @Test
    void crearServicio_conDatosValidos_retornaServicioResponseDto() {
        ServicioCreateDto createDto = new ServicioCreateDto();
        createDto.setClienteId(1L);
        createDto.setEquipoId(1L);
        createDto.setEmpleadoRecepcionId(1L);
        createDto.setTipoIngreso(TipoIngreso.CLIENTE_TRAE);
        createDto.setFallaReportada("No enciende");
        createDto.setEsGarantia(false);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(servicioRepository.findMaxNumeroServicio(any())).thenReturn(null);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);
        when(presupuestoService.generarNumeroPresupuesto()).thenReturn("PRE2600001");
        when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(new Presupuesto());

        ServicioResponseDto resultado = servicioService.crearServicio(createDto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("SRV2600001", resultado.getNumeroServicio());
        verify(notificationService).notificarServicioCreado(any(ServicioListDto.class));
        verify(presupuestoRepository).save(any(Presupuesto.class)); // Presupuesto auto
    }

    @Test
    void crearServicio_esGarantia_noCreaPresupuesto() {
        ServicioCreateDto createDto = new ServicioCreateDto();
        createDto.setClienteId(1L);
        createDto.setEquipoId(1L);
        createDto.setEmpleadoRecepcionId(1L);
        createDto.setTipoIngreso(TipoIngreso.CLIENTE_TRAE);
        createDto.setEsGarantia(true);
        createDto.setServicioGarantiaId(2L);

        Servicio servicioOriginal = new Servicio();
        servicioOriginal.setId(2L);
        servicioOriginal.setFechaDevolucionReal(LocalDate.now().minusDays(10));

        Servicio servicioGarantia = new Servicio();
        servicioGarantia.setId(3L);
        servicioGarantia.setNumeroServicio("GTA2600001");
        servicioGarantia.setCliente(cliente);
        servicioGarantia.setEquipo(equipo);
        servicioGarantia.setEmpleadoRecepcion(empleado);
        servicioGarantia.setTipoIngreso(TipoIngreso.CLIENTE_TRAE);
        servicioGarantia.setEstado(EstadoServicio.RECIBIDO);
        servicioGarantia.setEsGarantia(true);
        servicioGarantia.setDetalleServicios(new ArrayList<>());
        servicioGarantia.setOrdenesTrabajo(new ArrayList<>());
        servicioGarantia.setFechaCreacion(LocalDateTime.now());
        servicioGarantia.setFechaRecepcion(LocalDate.now());

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(servicioRepository.findById(2L)).thenReturn(Optional.of(servicioOriginal));
        when(servicioRepository.findMaxNumeroServicio(any())).thenReturn(null);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicioGarantia);

        ServicioResponseDto resultado = servicioService.crearServicio(createDto);

        assertNotNull(resultado);
        verify(presupuestoRepository, never()).save(any(Presupuesto.class));
    }

    @Test
    void crearServicio_conClienteInexistente_lanzaRuntimeException() {
        ServicioCreateDto createDto = new ServicioCreateDto();
        createDto.setClienteId(99L);
        createDto.setEquipoId(1L);
        createDto.setEmpleadoRecepcionId(1L);

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> servicioService.crearServicio(createDto));
    }

    @Test
    void obtenerServicioPorId_conIdExistente_retornaServicioResponseDto() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        ServicioResponseDto resultado = servicioService.obtenerServicioPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerServicioPorId_conIdInexistente_lanzaServicioNotFoundException() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ServicioNotFoundException.class, () -> servicioService.obtenerServicioPorId(99L));
    }

    @Test
    void obtenerServicios_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Servicio> page = new PageImpl<>(List.of(servicio));
        when(servicioRepository.findByActivoTrue(pageable)).thenReturn(page);

        Page<ServicioListDto> resultado = servicioService.obtenerServicios(pageable);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void obtenerServiciosPorEstado_retornaLista() {
        when(servicioRepository.findByEstadoAndActivoTrue(EstadoServicio.RECIBIDO)).thenReturn(List.of(servicio));

        List<ServicioListDto> resultado = servicioService.obtenerServiciosPorEstado(EstadoServicio.RECIBIDO);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerServiciosPorCliente_retornaLista() {
        when(servicioRepository.findByClienteIdAndActivoTrue(1L)).thenReturn(List.of(servicio));

        List<ServicioListDto> resultado = servicioService.obtenerServiciosPorCliente(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerServicioPorNumero_conNumeroExistente_retornaServicioResponseDto() {
        when(servicioRepository.findByNumeroServicio("SRV2600001")).thenReturn(Optional.of(servicio));

        ServicioResponseDto resultado = servicioService.obtenerServicioPorNumero("SRV2600001");

        assertNotNull(resultado);
        assertEquals("SRV2600001", resultado.getNumeroServicio());
    }

    @Test
    void obtenerServicioPorNumero_conNumeroInexistente_lanzaServicioNotFoundException() {
        when(servicioRepository.findByNumeroServicio("INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(ServicioNotFoundException.class, () -> servicioService.obtenerServicioPorNumero("INEXISTENTE"));
    }

    @Test
    void actualizarServicio_conDatosValidos_retornaServicioActualizado() {
        ServicioUpdateDto updateDto = new ServicioUpdateDto();
        updateDto.setEstado(EstadoServicio.TERMINADO);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        ServicioResponseDto resultado = servicioService.actualizarServicio(1L, updateDto);

        assertNotNull(resultado);
        assertNotNull(servicio.getFechaDevolucionReal()); // Autoestablecida al cambiar a TERMINADO
        verify(notificationService).notificarServicioActualizado(any(ServicioListDto.class));
    }

    @Test
    void actualizarServicio_conIdInexistente_lanzaServicioNotFoundException() {
        ServicioUpdateDto updateDto = new ServicioUpdateDto();
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ServicioNotFoundException.class, () -> servicioService.actualizarServicio(99L, updateDto));
    }

    @Test
    void cambiarEstadoServicio_aTerminado_estableceFechaDevolucion() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        ServicioResponseDto resultado = servicioService.cambiarEstadoServicio(1L, EstadoServicio.TERMINADO);

        assertNotNull(resultado);
        assertEquals(EstadoServicio.TERMINADO, servicio.getEstado());
        assertNotNull(servicio.getFechaDevolucionReal());
        verify(notificationService).notificarCambioEstado(any(ServicioListDto.class), eq(EstadoServicio.RECIBIDO));
    }

    @Test
    void cambiarEstadoServicio_noTerminado_noEstableceFechaDevolucion() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        servicioService.cambiarEstadoServicio(1L, EstadoServicio.PRESUPUESTADO);

        assertNull(servicio.getFechaDevolucionReal());
    }

    @Test
    void eliminarServicio_conIdExistente_desactivaServicioYNotifica() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        servicioService.eliminarServicio(1L);

        assertFalse(servicio.getActivo());
        verify(servicioRepository).save(servicio);
        verify(notificationService).notificarServicioEliminado(1L);
    }

    @Test
    void eliminarServicio_conIdInexistente_lanzaServicioNotFoundException() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ServicioNotFoundException.class, () -> servicioService.eliminarServicio(99L));
    }

    @Test
    void generarNumeroServicio_sinServiciosPrevios_retornaPrimerNumero() {
        when(servicioRepository.findMaxNumeroServicio(any())).thenReturn(null);

        String numero = servicioService.generarNumeroServicio();

        assertNotNull(numero);
        assertTrue(numero.startsWith("SRV"));
        assertTrue(numero.endsWith("00001"));
    }

    @Test
    void generarNumeroServicio_conServiciosPrevios_retornaSiguienteNumero() {
        when(servicioRepository.findMaxNumeroServicio(any())).thenReturn(10);

        String numero = servicioService.generarNumeroServicio();

        assertNotNull(numero);
        assertTrue(numero.endsWith("00011"));
    }

    @Test
    void obtenerServiciosGarantia_retornaLista() {
        servicio.setEsGarantia(true);
        when(servicioRepository.findServiciosGarantiaActivos()).thenReturn(List.of(servicio));

        List<ServicioListDto> resultado = servicioService.obtenerServiciosGarantia();

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerItemsServicioOriginal_conServicioDeGarantia_retornaItems() {
        // Setup servicio original con orden de trabajo
        DetalleOrdenTrabajo detalle = new DetalleOrdenTrabajo();
        detalle.setId(1L);
        detalle.setItemDescripcion("Pantalla LCD");
        detalle.setCantidad(1);
        detalle.setCompletado(true);

        OrdenTrabajo ordenOriginal = new OrdenTrabajo();
        ordenOriginal.setId(1L);
        ordenOriginal.setEstado(EstadoOrdenTrabajo.TERMINADA);
        ordenOriginal.setDetalleOrdenesTrabajo(List.of(detalle));

        Servicio servicioOriginal = new Servicio();
        servicioOriginal.setId(2L);
        servicioOriginal.setOrdenesTrabajo(List.of(ordenOriginal));

        Servicio servicioGarantia = new Servicio();
        servicioGarantia.setId(3L);
        servicioGarantia.setEsGarantia(true);
        servicioGarantia.setServicioGarantia(servicioOriginal);

        when(servicioRepository.findById(3L)).thenReturn(Optional.of(servicioGarantia));

        List<ItemServicioOriginalDto> resultado = servicioService.obtenerItemsServicioOriginal(3L);

        assertEquals(1, resultado.size());
        assertEquals("Pantalla LCD", resultado.get(0).getItem());
    }

    @Test
    void obtenerItemsServicioOriginal_noEsGarantia_lanzaRuntimeException() {
        Servicio noGarantia = new Servicio();
        noGarantia.setId(1L);
        noGarantia.setEsGarantia(false);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(noGarantia));

        assertThrows(RuntimeException.class, () -> servicioService.obtenerItemsServicioOriginal(1L));
    }

    @Test
    void obtenerServiciosPorFechas_retornaLista() {
        LocalDate inicio = LocalDate.now().minusDays(7);
        LocalDate fin = LocalDate.now();

        when(servicioRepository.findByFechaRecepcionBetweenAndActivoTrue(inicio, fin)).thenReturn(List.of(servicio));

        List<ServicioListDto> resultado = servicioService.obtenerServiciosPorFechas(inicio, fin);

        assertEquals(1, resultado.size());
    }
}
