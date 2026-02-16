package com.sigret.services.impl;

import com.sigret.entities.Presupuesto;
import com.sigret.entities.PresupuestoToken;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.repositories.PresupuestoTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresupuestoTokenServiceImplTest {

    @Mock
    private PresupuestoTokenRepository tokenRepository;

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @InjectMocks
    private PresupuestoTokenServiceImpl tokenService;

    private Presupuesto presupuesto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "tokenExpirationDays", 7);

        presupuesto = new Presupuesto();
        presupuesto.setId(1L);
        presupuesto.setNumeroPresupuesto("PRE2600001");
        presupuesto.setFechaVencimiento(LocalDate.now().plusDays(7));
    }

    @Test
    void generarToken_conPresupuestoValido_retornaToken() {
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(tokenRepository.save(any(PresupuestoToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = tokenService.generarToken(1L, "APROBAR", "ORIGINAL");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(tokenRepository).save(any(PresupuestoToken.class));
    }

    @Test
    void generarToken_sinTipoPrecio_retornaToken() {
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));
        when(tokenRepository.save(any(PresupuestoToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = tokenService.generarToken(1L, "RECHAZAR");

        assertNotNull(token);
        verify(tokenRepository).save(any(PresupuestoToken.class));
    }

    @Test
    void generarToken_conPresupuestoInexistente_lanzaRuntimeException() {
        when(presupuestoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> tokenService.generarToken(99L, "APROBAR", "ORIGINAL"));
    }

    @Test
    void generarToken_sinFechaVencimiento_lanzaRuntimeException() {
        presupuesto.setFechaVencimiento(null);
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(presupuesto));

        assertThrows(RuntimeException.class, () -> tokenService.generarToken(1L, "APROBAR", "ORIGINAL"));
    }

    @Test
    void validarToken_conTokenValido_retornaPresupuestoToken() {
        PresupuestoToken token = new PresupuestoToken();
        token.setId(1L);
        token.setToken("test-token-uuid");
        token.setPresupuesto(presupuesto);
        token.setUsado(false);
        token.setFechaExpiracion(LocalDateTime.now().plusDays(7));

        when(tokenRepository.findByToken("test-token-uuid")).thenReturn(Optional.of(token));

        PresupuestoToken resultado = tokenService.validarToken("test-token-uuid");

        assertNotNull(resultado);
        assertEquals("test-token-uuid", resultado.getToken());
    }

    @Test
    void validarToken_conTokenInexistente_lanzaRuntimeException() {
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> tokenService.validarToken("invalid-token"));
    }

    @Test
    void validarToken_conTokenYaUsado_lanzaRuntimeException() {
        PresupuestoToken token = new PresupuestoToken();
        token.setToken("used-token");
        token.setUsado(true);

        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThrows(RuntimeException.class, () -> tokenService.validarToken("used-token"));
    }

    @Test
    void validarToken_conTokenExpirado_lanzaRuntimeException() {
        PresupuestoToken token = new PresupuestoToken();
        token.setToken("expired-token");
        token.setUsado(false);
        token.setFechaExpiracion(LocalDateTime.now().minusDays(1));

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(RuntimeException.class, () -> tokenService.validarToken("expired-token"));
    }

    @Test
    void marcarTokenComoUsado_conTokenExistente_actualizaToken() {
        PresupuestoToken token = new PresupuestoToken();
        token.setToken("test-token");
        token.setUsado(false);

        when(tokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(PresupuestoToken.class))).thenReturn(token);

        tokenService.marcarTokenComoUsado("test-token", "192.168.1.1");

        assertTrue(token.getUsado());
        assertNotNull(token.getFechaUso());
        assertEquals("192.168.1.1", token.getIpUso());
    }

    @Test
    void marcarTokenComoUsado_conTokenInexistente_lanzaRuntimeException() {
        when(tokenRepository.findByToken("inexistente")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> tokenService.marcarTokenComoUsado("inexistente", "192.168.1.1"));
    }

    @Test
    void limpiarTokensExpirados_conTokensExpirados_eliminaTokens() {
        PresupuestoToken tokenExpirado = new PresupuestoToken();
        tokenExpirado.setId(1L);
        tokenExpirado.setFechaExpiracion(LocalDateTime.now().minusDays(1));

        when(tokenRepository.findExpiredTokens(any(LocalDateTime.class))).thenReturn(List.of(tokenExpirado));

        tokenService.limpiarTokensExpirados();

        verify(tokenRepository).deleteAll(List.of(tokenExpirado));
    }

    @Test
    void limpiarTokensExpirados_sinTokensExpirados_noEliminaNada() {
        when(tokenRepository.findExpiredTokens(any(LocalDateTime.class))).thenReturn(List.of());

        tokenService.limpiarTokensExpirados();

        verify(tokenRepository, never()).deleteAll(any());
    }

    @Test
    void invalidarTokensAnteriores_eliminaTokensNoUsados() {
        tokenService.invalidarTokensAnteriores(1L);

        verify(tokenRepository).deleteByPresupuestoIdAndUsadoFalse(1L);
    }
}
