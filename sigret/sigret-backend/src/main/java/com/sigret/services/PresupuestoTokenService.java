package com.sigret.services;

import com.sigret.entities.PresupuestoToken;

public interface PresupuestoTokenService {

    String generarToken(Long presupuestoId, String tipoAccion);

    PresupuestoToken validarToken(String token);

    void marcarTokenComoUsado(String token, String ip);

    void limpiarTokensExpirados();

    void invalidarTokensAnteriores(Long presupuestoId);
}