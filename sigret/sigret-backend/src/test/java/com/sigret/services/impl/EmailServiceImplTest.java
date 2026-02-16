package com.sigret.services.impl;

import com.sigret.entities.*;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.services.PresupuestoTokenService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private PresupuestoTokenService tokenService;

    @InjectMocks
    private EmailServiceImpl emailService;

    private MimeMessage mimeMessage;
    private Presupuesto presupuesto;
    private Servicio servicio;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@sigret.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");

        mimeMessage = mock(MimeMessage.class);

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

        servicio = new Servicio();
        servicio.setId(1L);
        servicio.setNumeroServicio("SER2600001");
        servicio.setCliente(cliente);
        servicio.setEquipo(equipo);

        presupuesto = new Presupuesto();
        presupuesto.setId(1L);
        presupuesto.setNumeroPresupuesto("PRE2600001");
        presupuesto.setServicio(servicio);
        presupuesto.setEstado(EstadoPresupuesto.LISTO);
        presupuesto.setMontoRepuestosOriginal(new BigDecimal("5000"));
        presupuesto.setManoObra(new BigDecimal("3000"));
        presupuesto.setMontoTotalOriginal(new BigDecimal("8000"));
        presupuesto.setFechaVencimiento(LocalDate.now().plusDays(7));
        presupuesto.setDiagnostico("Pantalla rota");
    }

    @Test
    void enviarEmail_conDatosValidos_enviaEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.enviarEmail("dest@mail.com", "Asunto", "Mensaje");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void enviarEmail_conError_lanzaRuntimeException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Error de conexión"));

        assertThrows(RuntimeException.class, () ->
                emailService.enviarEmail("dest@mail.com", "Asunto", "Mensaje"));
    }

    @Test
    void enviarEmailConAdjunto_conDatosValidos_enviaEmailConAdjunto() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        byte[] adjunto = "contenido".getBytes();

        emailService.enviarEmailConAdjunto("dest@mail.com", "Asunto", "Mensaje", adjunto, "archivo.pdf");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void enviarPresupuestoACliente_conPresupuestoValido_enviaEmail() {
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(tokenService.generarToken(eq(1L), eq("APROBAR"), eq("ORIGINAL"))).thenReturn("token-aprobar-original");
        when(tokenService.generarToken(eq(1L), eq("RECHAZAR"), isNull())).thenReturn("token-rechazar");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.enviarPresupuestoACliente(1L, true, false, null);

        verify(tokenService).invalidarTokensAnteriores(1L);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void enviarPresupuestoACliente_conAmbosPrecios_generaDosTokensAprobacion() {
        presupuesto.setMontoRepuestosAlternativo(new BigDecimal("3000"));
        presupuesto.setMontoTotalAlternativo(new BigDecimal("6000"));

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(tokenService.generarToken(eq(1L), eq("APROBAR"), eq("ORIGINAL"))).thenReturn("token-original");
        when(tokenService.generarToken(eq(1L), eq("APROBAR"), eq("ALTERNATIVO"))).thenReturn("token-alternativo");
        when(tokenService.generarToken(eq(1L), eq("RECHAZAR"), isNull())).thenReturn("token-rechazar");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.enviarPresupuestoACliente(1L, true, true, "Mensaje adicional");

        verify(tokenService).generarToken(1L, "APROBAR", "ORIGINAL");
        verify(tokenService).generarToken(1L, "APROBAR", "ALTERNATIVO");
        verify(tokenService).generarToken(1L, "RECHAZAR", null);
    }

    @Test
    void enviarPresupuestoACliente_conPresupuestoInexistente_lanzaRuntimeException() {
        when(presupuestoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                emailService.enviarPresupuestoACliente(99L, true, false, null));
    }

    @Test
    void enviarPresupuestoACliente_sinEmailCliente_lanzaRuntimeException() {
        cliente.getPersona().setContactos(new ArrayList<>());

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));

        assertThrows(RuntimeException.class, () ->
                emailService.enviarPresupuestoACliente(1L, true, false, null));
    }

    @Test
    void enviarPresupuestoACliente_sinFechaVencimiento_lanzaRuntimeException() {
        presupuesto.setFechaVencimiento(null);

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));

        assertThrows(RuntimeException.class, () ->
                emailService.enviarPresupuestoACliente(1L, true, false, null));
    }

    @Test
    void enviarPresupuestoACliente_conFechaVencimientoPasada_lanzaRuntimeException() {
        presupuesto.setFechaVencimiento(LocalDate.now().minusDays(1));

        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));

        assertThrows(RuntimeException.class, () ->
                emailService.enviarPresupuestoACliente(1L, true, false, null));
    }
}
