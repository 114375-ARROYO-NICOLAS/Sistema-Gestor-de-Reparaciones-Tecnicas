package com.sigret.services.impl;

import com.sigret.entities.Presupuesto;
import com.sigret.entities.PresupuestoToken;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.repositories.PresupuestoTokenRepository;
import com.sigret.services.PresupuestoTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresupuestoTokenServiceImpl implements PresupuestoTokenService {

    private final PresupuestoTokenRepository tokenRepository;
    private final PresupuestoRepository presupuestoRepository;

    @Value("${sigret.presupuesto.token-expiration-days:7}")
    private int tokenExpirationDays;

    @Override
    @Transactional
    public String generarToken(Long presupuestoId, String tipoAccion) {
        return generarToken(presupuestoId, tipoAccion, null);
    }

    @Transactional
    public String generarToken(Long presupuestoId, String tipoAccion, String tipoPrecio) {
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        // Validar que el presupuesto tenga fecha de vencimiento
        if (presupuesto.getFechaVencimiento() == null) {
            throw new RuntimeException("El presupuesto debe tener una fecha de vencimiento para generar el token");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        // Sincronizar expiración del token con la fecha de vencimiento del presupuesto
        LocalDateTime expiracion = presupuesto.getFechaVencimiento().atTime(23, 59, 59);

        PresupuestoToken presupuestoToken = new PresupuestoToken();
        presupuestoToken.setToken(token);
        presupuestoToken.setPresupuesto(presupuesto);
        presupuestoToken.setTipoAccion(tipoAccion);
        presupuestoToken.setTipoPrecio(tipoPrecio);
        presupuestoToken.setFechaExpiracion(expiracion);
        presupuestoToken.setUsado(false);
        presupuestoToken.setFechaCreacion(now);

        tokenRepository.save(presupuestoToken);

        return token;
    }

    @Override
    public PresupuestoToken validarToken(String token) {
        PresupuestoToken presupuestoToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o no encontrado"));

        if (presupuestoToken.getUsado()) {
            throw new RuntimeException("Este token ya ha sido utilizado");
        }

        if (presupuestoToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Este token ha expirado");
        }

        return presupuestoToken;
    }

    @Override
    @Transactional
    public void marcarTokenComoUsado(String token, String ip) {
        PresupuestoToken presupuestoToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token no encontrado"));

        presupuestoToken.setUsado(true);
        presupuestoToken.setFechaUso(LocalDateTime.now());
        presupuestoToken.setIpUso(ip);

        tokenRepository.save(presupuestoToken);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * *") // Ejecutar todos los días a las 2 AM
    public void limpiarTokensExpirados() {
        List<PresupuestoToken> tokensExpirados = tokenRepository.findExpiredTokens(LocalDateTime.now());
        if (!tokensExpirados.isEmpty()) {
            tokenRepository.deleteAll(tokensExpirados);
        }
    }

    @Override
    @Transactional
    public void invalidarTokensAnteriores(Long presupuestoId) {
        tokenRepository.deleteByPresupuestoIdAndUsadoFalse(presupuestoId);
    }
}