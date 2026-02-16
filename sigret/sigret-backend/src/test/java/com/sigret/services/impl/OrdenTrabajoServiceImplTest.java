package com.sigret.services.impl;

import com.sigret.dtos.ordenTrabajo.*;
import com.sigret.dtos.servicio.ItemEvaluacionGarantiaDto;
import com.sigret.entities.*;
import com.sigret.enums.EstadoOrdenTrabajo;
import com.sigret.enums.EstadoServicio;
import com.sigret.exception.OrdenTrabajoNotFoundException;
import com.sigret.repositories.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenTrabajoServiceImplTest {

    @Mock
    private OrdenTrabajoRepository ordenTrabajoRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private EmpleadoRepository empleadoRepository;

    @Mock
    private RepuestoRepository repuestoRepository;

    @Mock
    private WebSocketNotificationService notificationService;

    @InjectMocks
    private OrdenTrabajoServiceImpl ordenTrabajoService;

    private Servicio servicio;
    private Empleado empleado;
    private Presupuesto presupuesto;
    private OrdenTrabajo ordenTrabajo;

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

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setPersona(personaCliente);

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
        servicio.setEstado(EstadoServicio.APROBADO);
        servicio.setEsGarantia(false);

        presupuesto = new Presupuesto();
        presupuesto.setId(1L);
        presupuesto.setNumeroPresupuesto("PRE2600001");
        presupuesto.setServicio(servicio);
        presupuesto.setDetallePresupuestos(new ArrayList<>());

        ordenTrabajo = new OrdenTrabajo();
        ordenTrabajo.setId(1L);
        ordenTrabajo.setNumeroOrdenTrabajo("ODT2600001");
        ordenTrabajo.setServicio(servicio);
        ordenTrabajo.setPresupuesto(presupuesto);
        ordenTrabajo.setEmpleado(empleado);
        ordenTrabajo.setMontoTotalRepuestos(BigDecimal.ZERO);
        ordenTrabajo.setMontoExtras(BigDecimal.ZERO);
        ordenTrabajo.setEsSinCosto(false);
        ordenTrabajo.setEstado(EstadoOrdenTrabajo.PENDIENTE);
        ordenTrabajo.setFechaCreacion(LocalDateTime.now());
        ordenTrabajo.setDetalleOrdenesTrabajo(new ArrayList<>());
    }

    @Test
    void crearOrdenTrabajo_conDatosValidos_retornaOrdenTrabajoResponseDto() {
        OrdenTrabajoCreateDto createDto = new OrdenTrabajoCreateDto();
        createDto.setServicioId(1L);
        createDto.setEmpleadoId(1L);
        createDto.setPresupuestoId(1L);
        createDto.setMontoTotalRepuestos(BigDecimal.ZERO);
        createDto.setMontoExtras(BigDecimal.ZERO);
        createDto.setEsSinCosto(false);
        createDto.setEstado(EstadoOrdenTrabajo.PENDIENTE);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(ordenTrabajoRepository.findMaxNumeroOrdenTrabajo(any())).thenReturn(null);
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenTrabajo);

        OrdenTrabajoResponseDto resultado = ordenTrabajoService.crearOrdenTrabajo(createDto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void crearOrdenTrabajo_sinPresupuesto_retornaOrdenTrabajoResponseDto() {
        OrdenTrabajoCreateDto createDto = new OrdenTrabajoCreateDto();
        createDto.setServicioId(1L);
        createDto.setEmpleadoId(1L);
        createDto.setPresupuestoId(null);
        createDto.setMontoTotalRepuestos(BigDecimal.ZERO);
        createDto.setMontoExtras(BigDecimal.ZERO);
        createDto.setEsSinCosto(true);
        createDto.setEstado(EstadoOrdenTrabajo.PENDIENTE);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(ordenTrabajoRepository.findMaxNumeroOrdenTrabajo(any())).thenReturn(null);
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenTrabajo);

        OrdenTrabajoResponseDto resultado = ordenTrabajoService.crearOrdenTrabajo(createDto);

        assertNotNull(resultado);
    }

    @Test
    void obtenerOrdenTrabajoPorId_conIdExistente_retornaOrdenTrabajoResponseDto() {
        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenTrabajo));

        OrdenTrabajoResponseDto resultado = ordenTrabajoService.obtenerOrdenTrabajoPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerOrdenTrabajoPorId_conIdInexistente_lanzaOrdenTrabajoNotFoundException() {
        when(ordenTrabajoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrdenTrabajoNotFoundException.class, () -> ordenTrabajoService.obtenerOrdenTrabajoPorId(99L));
    }

    @Test
    void obtenerOrdenesTrabajo_retornaPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdenTrabajo> page = new PageImpl<>(List.of(ordenTrabajo));
        when(ordenTrabajoRepository.findAll(pageable)).thenReturn(page);

        Page<OrdenTrabajoListDto> resultado = ordenTrabajoService.obtenerOrdenesTrabajo(pageable);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void obtenerOrdenesTrabajoPorEstado_retornaLista() {
        when(ordenTrabajoRepository.findByEstado(EstadoOrdenTrabajo.PENDIENTE)).thenReturn(List.of(ordenTrabajo));

        List<OrdenTrabajoListDto> resultado = ordenTrabajoService.obtenerOrdenesTrabajoPorEstado(EstadoOrdenTrabajo.PENDIENTE);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerOrdenesTrabajoPorEmpleado_retornaLista() {
        when(ordenTrabajoRepository.findByEmpleadoId(1L)).thenReturn(List.of(ordenTrabajo));

        List<OrdenTrabajoListDto> resultado = ordenTrabajoService.obtenerOrdenesTrabajoPorEmpleado(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerOrdenesTrabajoPorServicio_retornaLista() {
        when(ordenTrabajoRepository.findByServicioId(1L)).thenReturn(List.of(ordenTrabajo));

        List<OrdenTrabajoListDto> resultado = ordenTrabajoService.obtenerOrdenesTrabajoPorServicio(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void actualizarOrdenTrabajo_conDatosValidos_retornaOrdenTrabajoActualizada() {
        OrdenTrabajoUpdateDto updateDto = new OrdenTrabajoUpdateDto();
        updateDto.setMontoExtras(new BigDecimal("2000"));
        updateDto.setObservacionesExtras("Extras de prueba");

        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenTrabajo));
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenTrabajo);

        OrdenTrabajoResponseDto resultado = ordenTrabajoService.actualizarOrdenTrabajo(1L, updateDto);

        assertNotNull(resultado);
    }

    @Test
    void cambiarEstadoOrdenTrabajo_actualizaEstado() {
        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenTrabajo));
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenTrabajo);

        OrdenTrabajoResponseDto resultado = ordenTrabajoService.cambiarEstadoOrdenTrabajo(1L, EstadoOrdenTrabajo.EN_PROGRESO);

        assertNotNull(resultado);
        assertEquals(EstadoOrdenTrabajo.EN_PROGRESO, ordenTrabajo.getEstado());
    }

    @Test
    void asignarEmpleado_conDatosValidos_asignaEmpleadoYNotifica() {
        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenTrabajo));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenTrabajo);

        OrdenTrabajoResponseDto resultado = ordenTrabajoService.asignarEmpleado(1L, 1L);

        assertNotNull(resultado);
        verify(notificationService).notificarOrdenTrabajo(any(OrdenTrabajoEventDto.class));
    }

    @Test
    void iniciarOrdenTrabajo_cambiaEstadoAEnProgreso() {
        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenTrabajo));
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenTrabajo);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        OrdenTrabajoResponseDto resultado = ordenTrabajoService.iniciarOrdenTrabajo(1L);

        assertNotNull(resultado);
        assertEquals(EstadoOrdenTrabajo.EN_PROGRESO, ordenTrabajo.getEstado());
        assertNotNull(ordenTrabajo.getFechaComienzo());
        assertEquals(EstadoServicio.EN_REPARACION, servicio.getEstado());
        verify(notificationService).notificarOrdenTrabajo(any(OrdenTrabajoEventDto.class));
    }

    @Test
    void finalizarOrdenTrabajo_cambiaEstadoATerminada() {
        ordenTrabajo.setEstado(EstadoOrdenTrabajo.EN_PROGRESO);

        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenTrabajo));
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenTrabajo);
        when(servicioRepository.save(any(Servicio.class))).thenReturn(servicio);

        OrdenTrabajoResponseDto resultado = ordenTrabajoService.finalizarOrdenTrabajo(1L);

        assertNotNull(resultado);
        assertEquals(EstadoOrdenTrabajo.TERMINADA, ordenTrabajo.getEstado());
        assertNotNull(ordenTrabajo.getFechaFin());
        assertEquals(EstadoServicio.TERMINADO, servicio.getEstado());
    }

    @Test
    void eliminarOrdenTrabajo_conIdExistente_eliminaOrdenTrabajo() {
        when(ordenTrabajoRepository.existsById(1L)).thenReturn(true);

        ordenTrabajoService.eliminarOrdenTrabajo(1L);

        verify(ordenTrabajoRepository).deleteById(1L);
    }

    @Test
    void eliminarOrdenTrabajo_conIdInexistente_lanzaOrdenTrabajoNotFoundException() {
        when(ordenTrabajoRepository.existsById(99L)).thenReturn(false);

        assertThrows(OrdenTrabajoNotFoundException.class, () -> ordenTrabajoService.eliminarOrdenTrabajo(99L));
    }

    @Test
    void generarNumeroOrdenTrabajo_sinOrdenesPrevias_retornaPrimerNumero() {
        when(ordenTrabajoRepository.findMaxNumeroOrdenTrabajo(any())).thenReturn(null);

        String numero = ordenTrabajoService.generarNumeroOrdenTrabajo();

        assertNotNull(numero);
        assertTrue(numero.startsWith("ODT"));
        assertTrue(numero.endsWith("00001"));
    }

    @Test
    void generarNumeroOrdenTrabajo_conOrdenesPrevias_retornaSiguienteNumero() {
        when(ordenTrabajoRepository.findMaxNumeroOrdenTrabajo(any())).thenReturn(3);

        String numero = ordenTrabajoService.generarNumeroOrdenTrabajo();

        assertNotNull(numero);
        assertTrue(numero.endsWith("00004"));
    }

    @Test
    void obtenerOrdenesTrabajoSinCosto_retornaLista() {
        ordenTrabajo.setEsSinCosto(true);
        when(ordenTrabajoRepository.findOrdenesTrabajoSinCosto()).thenReturn(List.of(ordenTrabajo));

        List<OrdenTrabajoListDto> resultado = ordenTrabajoService.obtenerOrdenesTrabajoSinCosto();

        assertEquals(1, resultado.size());
    }

    @Test
    void crearOrdenTrabajoGarantia_conDatosValidos_retornaOrdenSinCosto() {
        servicio.setEsGarantia(true);

        Repuesto repuesto = new Repuesto();
        repuesto.setId(1L);
        repuesto.setDescripcion("Pantalla LCD");
        TipoEquipo te = new TipoEquipo();
        te.setDescripcion("Notebook");
        repuesto.setTipoEquipo(te);

        ItemEvaluacionGarantiaDto itemDto = new ItemEvaluacionGarantiaDto();
        itemDto.setRepuestoId(1L);
        itemDto.setComentario("Defectuoso");

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(empleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(ordenTrabajoRepository.findMaxNumeroOrdenTrabajo(any())).thenReturn(null);
        when(repuestoRepository.findById(1L)).thenReturn(Optional.of(repuesto));
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenTrabajo);

        OrdenTrabajoResponseDto resultado = ordenTrabajoService.crearOrdenTrabajoGarantia(
                1L, 1L, "Observaciones garantia", List.of(itemDto));

        assertNotNull(resultado);
        verify(notificationService).notificarOrdenTrabajo(any(OrdenTrabajoEventDto.class));
    }

    @Test
    void crearOrdenTrabajoGarantia_sinSerGarantia_lanzaRuntimeException() {
        servicio.setEsGarantia(false);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        assertThrows(RuntimeException.class, () ->
                ordenTrabajoService.crearOrdenTrabajoGarantia(1L, 1L, "obs", List.of()));
    }

    @Test
    void actualizarDetalleOrdenTrabajo_conDatosValidos_actualizaDetalle() {
        DetalleOrdenTrabajo detalle = new DetalleOrdenTrabajo();
        detalle.setId(1L);
        detalle.setItemDescripcion("Pantalla");
        detalle.setCantidad(1);
        detalle.setCompletado(false);
        ordenTrabajo.getDetalleOrdenesTrabajo().add(detalle);

        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenTrabajo));
        when(ordenTrabajoRepository.save(any(OrdenTrabajo.class))).thenReturn(ordenTrabajo);

        OrdenTrabajoResponseDto resultado = ordenTrabajoService.actualizarDetalleOrdenTrabajo(1L, 1L, "Comentario", true);

        assertNotNull(resultado);
        assertEquals("Comentario", detalle.getComentario());
        assertTrue(detalle.getCompletado());
    }

    @Test
    void todosLosDetallesCompletados_todosCompletados_retornaTrue() {
        DetalleOrdenTrabajo detalle1 = new DetalleOrdenTrabajo();
        detalle1.setCompletado(true);
        DetalleOrdenTrabajo detalle2 = new DetalleOrdenTrabajo();
        detalle2.setCompletado(true);

        ordenTrabajo.getDetalleOrdenesTrabajo().addAll(List.of(detalle1, detalle2));

        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenTrabajo));

        assertTrue(ordenTrabajoService.todosLosDetallesCompletados(1L));
    }

    @Test
    void todosLosDetallesCompletados_algunoIncompleto_retornaFalse() {
        DetalleOrdenTrabajo detalle1 = new DetalleOrdenTrabajo();
        detalle1.setCompletado(true);
        DetalleOrdenTrabajo detalle2 = new DetalleOrdenTrabajo();
        detalle2.setCompletado(false);

        ordenTrabajo.getDetalleOrdenesTrabajo().addAll(List.of(detalle1, detalle2));

        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenTrabajo));

        assertFalse(ordenTrabajoService.todosLosDetallesCompletados(1L));
    }

    @Test
    void todosLosDetallesCompletados_sinDetalles_retornaTrue() {
        when(ordenTrabajoRepository.findById(1L)).thenReturn(Optional.of(ordenTrabajo));

        assertTrue(ordenTrabajoService.todosLosDetallesCompletados(1L));
    }
}
