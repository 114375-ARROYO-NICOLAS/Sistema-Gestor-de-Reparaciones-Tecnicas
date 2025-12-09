package com.sigret.services;

public interface EmailService {

    void enviarPresupuestoACliente(Long presupuestoId, Boolean mostrarOriginal,
                                    Boolean mostrarAlternativo, String mensajeAdicional);
}