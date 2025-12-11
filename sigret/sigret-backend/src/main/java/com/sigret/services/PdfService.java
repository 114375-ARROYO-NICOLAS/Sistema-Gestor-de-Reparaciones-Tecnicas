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
}
