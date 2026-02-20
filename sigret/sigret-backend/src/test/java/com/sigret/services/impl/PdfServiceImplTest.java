package com.sigret.services.impl;

import com.sigret.entities.*;
import com.sigret.enums.TipoIngreso;
import com.sigret.repositories.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfServiceImplTest {

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private PdfServiceImpl pdfService;

    private Servicio servicio;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        TipoPersona tipoPersona = new TipoPersona();
        tipoPersona.setId(1L);
        tipoPersona.setDescripcion("Física");

        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setId(1L);
        tipoDocumento.setDescripcion("DNI");

        TipoContacto tipoContacto = new TipoContacto();
        tipoContacto.setId(1L);
        tipoContacto.setDescripcion("Email");

        Contacto contacto = new Contacto();
        contacto.setId(1L);
        contacto.setTipoContacto(tipoContacto);
        contacto.setDescripcion("cliente@mail.com");

        Persona persona = new Persona();
        persona.setId(1L);
        persona.setNombre("Juan");
        persona.setApellido("Pérez");
        persona.setTipoPersona(tipoPersona);
        persona.setTipoDocumento(tipoDocumento);
        persona.setDocumento("12345678");
        persona.setContactos(new ArrayList<>());
        persona.getContactos().add(contacto);
        contacto.setPersona(persona);

        cliente = new Cliente(persona);
        cliente.setId(1L);
        cliente.setActivo(true);

        TipoEquipo tipoEquipo = new TipoEquipo();
        tipoEquipo.setId(1L);
        tipoEquipo.setDescripcion("Notebook");

        Marca marca = new Marca();
        marca.setId(1L);
        marca.setDescripcion("Dell");

        Modelo modelo = new Modelo();
        modelo.setId(1L);
        modelo.setDescripcion("Inspiron 15");

        Equipo equipo = new Equipo();
        equipo.setId(1L);
        equipo.setTipoEquipo(tipoEquipo);
        equipo.setMarca(marca);
        equipo.setModelo(modelo);
        equipo.setNumeroSerie("SN123456");
        equipo.setColor("Negro");

        Persona personaEmpleado = new Persona();
        personaEmpleado.setId(2L);
        personaEmpleado.setNombre("Carlos");
        personaEmpleado.setApellido("González");
        personaEmpleado.setTipoPersona(tipoPersona);
        personaEmpleado.setTipoDocumento(tipoDocumento);

        Empleado empleado = new Empleado();
        empleado.setId(1L);
        empleado.setPersona(personaEmpleado);

        servicio = new Servicio();
        servicio.setId(1L);
        servicio.setNumeroServicio("SER2600001");
        servicio.setCliente(cliente);
        servicio.setEquipo(equipo);
        servicio.setEmpleadoRecepcion(empleado);
        servicio.setTipoIngreso(TipoIngreso.CLIENTE_TRAE);
        servicio.setFechaRecepcion(LocalDate.now());
        servicio.setFallaReportada("Pantalla rota");
        servicio.setEsGarantia(false);
        servicio.setAbonaVisita(false);
        servicio.setDetalleServicios(new ArrayList<>());
    }

    @Test
    void generarPdfServicio_conServicioValido_retornaBytes() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        byte[] resultado = pdfService.generarPdfServicio(1L);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    void generarPdfServicio_conServicioInexistente_lanzaRuntimeException() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> pdfService.generarPdfServicio(99L));
    }

    @Test
    void generarPdfServicio_conDetallesServicio_retornaBytes() {
        DetalleServicio detalle = new DetalleServicio();
        detalle.setId(1L);
        detalle.setComponente("Cargador");
        detalle.setPresente(true);
        detalle.setComentario("En buen estado");

        servicio.setDetalleServicios(new ArrayList<>());
        servicio.getDetalleServicios().add(detalle);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        byte[] resultado = pdfService.generarPdfServicio(1L);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

}
