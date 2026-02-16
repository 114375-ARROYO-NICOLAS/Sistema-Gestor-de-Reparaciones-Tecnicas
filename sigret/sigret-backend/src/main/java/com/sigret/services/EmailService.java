package com.sigret.services;

public interface EmailService {

    /**
     * Envía un email simple
     * @param destinatario email del destinatario
     * @param asunto asunto del email
     * @param mensaje cuerpo del mensaje
     */
    void enviarEmail(String destinatario, String asunto, String mensaje);

    /**
     * Envía un email con archivo adjunto
     * @param destinatario email del destinatario
     * @param asunto asunto del email
     * @param mensaje cuerpo del mensaje
     * @param adjunto bytes del archivo adjunto
     * @param nombreAdjunto nombre del archivo adjunto
     */
    void enviarEmailConAdjunto(String destinatario, String asunto, String mensaje, byte[] adjunto, String nombreAdjunto);

    /**
     * Envía presupuesto a cliente (método legacy para compatibilidad)
     * @param presupuestoId ID del presupuesto
     * @param mostrarOriginal mostrar presupuesto original
     * @param mostrarAlternativo mostrar presupuesto alternativo
     * @param mensajeAdicional mensaje adicional
     */
    void enviarPresupuestoACliente(Long presupuestoId, Boolean mostrarOriginal, Boolean mostrarAlternativo, String mensajeAdicional);
}
