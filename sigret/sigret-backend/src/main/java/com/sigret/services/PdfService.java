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
     * Genera un PDF con el detalle del presupuesto para adjuntar al email del cliente
     * @param presupuestoId ID del presupuesto
     * @param mostrarOriginal si se muestra el precio con repuestos originales
     * @param mostrarAlternativo si se muestra el precio con repuestos alternativos
     * @return byte array con el PDF generado
     */
    byte[] generarPdfPresupuesto(Long presupuestoId, Boolean mostrarOriginal, Boolean mostrarAlternativo);

    /**
     * Genera un PDF final con info del servicio, presupuesto aprobado, orden de trabajo y firma de conformidad
     * @param servicioId ID del servicio
     * @return byte array con el PDF generado
     */
    byte[] generarPdfFinal(Long servicioId);
}
