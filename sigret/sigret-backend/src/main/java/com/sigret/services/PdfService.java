package com.sigret.services;

import com.sigret.entities.Servicio;

public interface PdfService {

    /**
     * Genera un PDF con la información del servicio
     * @param servicioId ID del servicio
     * @return byte array con el PDF generado
     */
    byte[] generarPdfServicio(Long servicioId);

    /**
     * Genera un PDF y lo envía por email al cliente
     * @param servicioId ID del servicio
     */
    void enviarPdfPorEmail(Long servicioId);

    /**
     * Genera un PDF final con info del servicio, presupuesto aprobado, orden de trabajo y firma de conformidad
     * @param servicioId ID del servicio
     * @return byte array con el PDF generado
     */
    byte[] generarPdfFinal(Long servicioId);

    /**
     * Genera un PDF final y lo envía por email al cliente
     * @param servicioId ID del servicio
     */
    void enviarPdfFinalPorEmail(Long servicioId);
}
