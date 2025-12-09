package com.sigret.services.impl;

import com.sigret.dtos.presupuesto.PresupuestoEventDto;
import com.sigret.entities.DetallePresupuesto;
import com.sigret.entities.Presupuesto;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.services.EmailService;
import com.sigret.services.PresupuestoService;
import com.sigret.services.PresupuestoTokenService;
import com.sigret.services.WebSocketNotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final PresupuestoRepository presupuestoRepository;
    private final PresupuestoTokenService tokenService;
    private final PresupuestoService presupuestoService;
    private final WebSocketNotificationService notificationService;

    @Value("${sigret.mail.from}")
    private String mailFrom;

    @Value("${sigret.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void enviarPresupuestoACliente(Long presupuestoId, Boolean mostrarOriginal,
                                           Boolean mostrarAlternativo, String mensajeAdicional) {
        // 1. Obtener presupuesto
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        // 2. Validar estado
        if (presupuesto.getEstado() != EstadoPresupuesto.LISTO) {
            throw new RuntimeException("El presupuesto debe estar en estado LISTO para ser enviado");
        }

        // 3. Validar cliente tiene email
        String emailCliente = presupuesto.getServicio().getCliente().getPrimerEmail();
        if (emailCliente == null || emailCliente.trim().isEmpty()) {
            throw new RuntimeException("El cliente no tiene un email registrado");
        }

        // 4. Invalidar tokens anteriores y generar nuevos
        tokenService.invalidarTokensAnteriores(presupuestoId);

        // Generar tokens según los precios a mostrar
        String tokenAprobacionOriginal = null;
        String tokenAprobacionAlternativo = null;

        if (mostrarOriginal && mostrarAlternativo) {
            // Si se muestran ambos precios, generar un token para cada uno
            tokenAprobacionOriginal = ((PresupuestoTokenServiceImpl) tokenService).generarToken(presupuestoId, "APROBAR", "ORIGINAL");
            tokenAprobacionAlternativo = ((PresupuestoTokenServiceImpl) tokenService).generarToken(presupuestoId, "APROBAR", "ALTERNATIVO");
        } else if (mostrarOriginal) {
            // Solo precio original
            tokenAprobacionOriginal = ((PresupuestoTokenServiceImpl) tokenService).generarToken(presupuestoId, "APROBAR", "ORIGINAL");
        } else {
            // Solo precio alternativo
            tokenAprobacionAlternativo = ((PresupuestoTokenServiceImpl) tokenService).generarToken(presupuestoId, "APROBAR", "ALTERNATIVO");
        }

        String tokenRechazo = tokenService.generarToken(presupuestoId, "RECHAZAR");

        // 5. Generar HTML del email
        String htmlContent = generarHtmlPresupuesto(presupuesto, tokenAprobacionOriginal,
                tokenAprobacionAlternativo, tokenRechazo, mostrarOriginal, mostrarAlternativo, mensajeAdicional);

        // 6. Enviar email
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(emailCliente);
            helper.setSubject("Presupuesto " + presupuesto.getNumeroPresupuesto() + " - SIGRET");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el email: " + e.getMessage(), e);
        }

        // 7. Actualizar presupuesto
        EstadoPresupuesto estadoAnterior = presupuesto.getEstado();
        presupuesto.setMostrarOriginal(mostrarOriginal);
        presupuesto.setMostrarAlternativo(mostrarAlternativo);
        presupuesto.setEstado(EstadoPresupuesto.ENVIADO);
        Presupuesto presupuestoActualizado = presupuestoRepository.save(presupuesto);

        // 8. Notificar via WebSocket
        PresupuestoEventDto evento = new PresupuestoEventDto();
        evento.setTipoEvento("CAMBIO_ESTADO");
        evento.setPresupuestoId(presupuestoActualizado.getId());
        evento.setServicioId(presupuestoActualizado.getServicio().getId());
        evento.setNumeroServicio(presupuestoActualizado.getServicio().getNumeroServicio());
        evento.setEstadoAnterior(estadoAnterior);
        evento.setEstadoNuevo(EstadoPresupuesto.ENVIADO);
        notificationService.notificarPresupuesto(evento);
    }

    private String generarHtmlPresupuesto(Presupuesto presupuesto, String tokenAprobacionOriginal,
                                           String tokenAprobacionAlternativo, String tokenRechazo,
                                           Boolean mostrarOriginal, Boolean mostrarAlternativo, String mensajeAdicional) {
        String nombreCliente = presupuesto.getServicio().getCliente().getNombreCompleto();
        String equipoDescripcion = presupuesto.getServicio().getEquipo().getDescripcionCompleta();
        String fallaReportada = presupuesto.getServicio().getFallaReportada();

        // Generar links de aprobación según los tokens disponibles
        String linkAprobacionOriginal = tokenAprobacionOriginal != null
            ? frontendUrl + "/p/" + tokenAprobacionOriginal + "/aprobar"
            : null;
        String linkAprobacionAlternativo = tokenAprobacionAlternativo != null
            ? frontendUrl + "/p/" + tokenAprobacionAlternativo + "/aprobar"
            : null;
        String linkRechazo = frontendUrl + "/p/" + tokenRechazo + "/rechazar";

        DecimalFormat formatter = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.forLanguageTag("es-AR")));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='es'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }");
        html.append(".container { max-width: 600px; margin: 20px auto; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(".header { background: #2c3e50; color: #fff; padding: 30px 20px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 24px; }");
        html.append(".content { padding: 30px 20px; }");
        html.append(".greeting { font-size: 18px; margin-bottom: 20px; }");
        html.append(".info-box { background: #ecf0f1; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        html.append(".info-box h3 { margin-top: 0; color: #2c3e50; font-size: 16px; }");
        html.append(".info-box p { margin: 5px 0; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th { background: #34495e; color: #fff; padding: 12px; text-align: left; }");
        html.append("td { padding: 10px; border-bottom: 1px solid #ddd; }");
        html.append("tr:hover { background: #f9f9f9; }");
        html.append(".total-row { background: #e8f4f8; font-weight: bold; }");
        html.append(".buttons { text-align: center; margin: 30px 0; }");
        html.append(".btn { display: inline-block; padding: 14px 30px; margin: 0 10px; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px; }");
        html.append(".btn-aprobar { background: #27ae60; color: #fff; }");
        html.append(".btn-rechazar { background: #e74c3c; color: #fff; }");
        html.append(".btn:hover { opacity: 0.9; }");
        html.append(".footer { background: #2c3e50; color: #95a5a6; padding: 20px; text-align: center; font-size: 12px; }");
        html.append(".mensaje-adicional { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>SIGRET - Sistema de Gestión</h1>");
        html.append("<p>Presupuesto ").append(presupuesto.getNumeroPresupuesto()).append("</p>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");
        html.append("<p class='greeting'>Estimado/a ").append(nombreCliente).append(",</p>");
        html.append("<p>Le enviamos el presupuesto solicitado para la reparación de su equipo.</p>");

        // Info del servicio
        html.append("<div class='info-box'>");
        html.append("<h3>Información del Servicio</h3>");
        html.append("<p><strong>Equipo:</strong> ").append(equipoDescripcion).append("</p>");
        html.append("<p><strong>Falla reportada:</strong> ").append(fallaReportada != null ? fallaReportada : "N/A").append("</p>");
        if (presupuesto.getDiagnostico() != null && !presupuesto.getDiagnostico().trim().isEmpty()) {
            html.append("<p><strong>Diagnóstico:</strong> ").append(presupuesto.getDiagnostico()).append("</p>");
        }
        html.append("<p><strong>Fecha:</strong> ").append(presupuesto.getFechaCreacion().format(dateFormatter)).append("</p>");
        html.append("</div>");

        // Mensaje adicional del propietario
        if (mensajeAdicional != null && !mensajeAdicional.trim().isEmpty()) {
            html.append("<div class='mensaje-adicional'>");
            html.append("<p><strong>Mensaje del taller:</strong></p>");
            html.append("<p>").append(mensajeAdicional).append("</p>");
            html.append("</div>");
        }

        // Tabla de detalles
        html.append("<h3>Detalle del Presupuesto</h3>");
        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>Ítem</th>");
        html.append("<th style='text-align: center;'>Cantidad</th>");

        if (mostrarOriginal && mostrarAlternativo) {
            html.append("<th style='text-align: right;'>Precio Original</th>");
            html.append("<th style='text-align: right;'>Precio Alternativo</th>");
        } else if (mostrarOriginal) {
            html.append("<th style='text-align: right;'>Precio Unitario</th>");
            html.append("<th style='text-align: right;'>Subtotal</th>");
        } else {
            html.append("<th style='text-align: right;'>Precio Unitario</th>");
            html.append("<th style='text-align: right;'>Subtotal</th>");
        }

        html.append("</tr></thead>");
        html.append("<tbody>");

        for (DetallePresupuesto detalle : presupuesto.getDetallePresupuestos()) {
            html.append("<tr>");
            html.append("<td>").append(detalle.getItem()).append("</td>");
            html.append("<td style='text-align: center;'>").append(detalle.getCantidad()).append("</td>");

            if (mostrarOriginal && mostrarAlternativo) {
                html.append("<td style='text-align: right;'>$ ").append(formatter.format(detalle.getSubtotalOriginal())).append("</td>");
                if (detalle.getPrecioAlternativo() != null) {
                    html.append("<td style='text-align: right;'>$ ").append(formatter.format(detalle.getSubtotalAlternativo())).append("</td>");
                } else {
                    html.append("<td style='text-align: right;'>-</td>");
                }
            } else if (mostrarOriginal) {
                html.append("<td style='text-align: right;'>$ ").append(formatter.format(detalle.getPrecioOriginal())).append("</td>");
                html.append("<td style='text-align: right;'>$ ").append(formatter.format(detalle.getSubtotalOriginal())).append("</td>");
            } else {
                BigDecimal precio = detalle.getPrecioAlternativo() != null ? detalle.getPrecioAlternativo() : detalle.getPrecioOriginal();
                BigDecimal subtotal = detalle.getPrecioAlternativo() != null ? detalle.getSubtotalAlternativo() : detalle.getSubtotalOriginal();
                html.append("<td style='text-align: right;'>$ ").append(formatter.format(precio)).append("</td>");
                html.append("<td style='text-align: right;'>$ ").append(formatter.format(subtotal)).append("</td>");
            }

            html.append("</tr>");
        }

        // Mano de obra
        if (presupuesto.getManoObra() != null && presupuesto.getManoObra().compareTo(BigDecimal.ZERO) > 0) {
            html.append("<tr>");
            html.append("<td colspan='").append(mostrarOriginal && mostrarAlternativo ? "2" : "3").append("' style='text-align: right;'><strong>Mano de Obra</strong></td>");
            html.append("<td style='text-align: right;'>$ ").append(formatter.format(presupuesto.getManoObra())).append("</td>");
            html.append("</tr>");
        }

        // Totales
        if (mostrarOriginal) {
            html.append("<tr class='total-row'>");
            html.append("<td colspan='").append(mostrarOriginal && mostrarAlternativo ? "2" : "3").append("' style='text-align: right;'><strong>Total ").append(mostrarOriginal && mostrarAlternativo ? "(Opción Original)" : "").append("</strong></td>");
            html.append("<td style='text-align: right;'><strong>$ ").append(formatter.format(presupuesto.getMontoTotalOriginal())).append("</strong></td>");
            html.append("</tr>");
        }

        if (mostrarAlternativo && presupuesto.getMontoTotalAlternativo() != null) {
            html.append("<tr class='total-row'>");
            html.append("<td colspan='").append(mostrarOriginal && mostrarAlternativo ? "2" : "3").append("' style='text-align: right;'><strong>Total ").append(mostrarOriginal && mostrarAlternativo ? "(Opción Alternativa)" : "").append("</strong></td>");
            html.append("<td style='text-align: right;'><strong>$ ").append(formatter.format(presupuesto.getMontoTotalAlternativo())).append("</strong></td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        // Botones de acción
        html.append("<div class='buttons'>");

        // Si se muestran ambos precios, mostrar botones separados
        if (mostrarOriginal && mostrarAlternativo) {
            html.append("<div style='margin-bottom: 15px;'>");
            html.append("<p style='font-weight: bold; margin-bottom: 10px;'>Seleccione la opción que desea aprobar:</p>");
            html.append("</div>");

            if (linkAprobacionOriginal != null) {
                html.append("<a href='").append(linkAprobacionOriginal).append("' class='btn btn-aprobar' style='background: #27ae60;'>Aprobar Precio Original</a>");
            }
            if (linkAprobacionAlternativo != null) {
                html.append("<a href='").append(linkAprobacionAlternativo).append("' class='btn btn-aprobar' style='background: #17a2b8;'>Aprobar Precio Alternativo</a>");
            }
        } else {
            // Si se muestra solo un precio, mostrar un botón genérico
            String linkAprobacion = linkAprobacionOriginal != null ? linkAprobacionOriginal : linkAprobacionAlternativo;
            if (linkAprobacion != null) {
                html.append("<a href='").append(linkAprobacion).append("' class='btn btn-aprobar'>Aprobar Presupuesto</a>");
            }
        }

        html.append("<a href='").append(linkRechazo).append("' class='btn btn-rechazar'>Rechazar Presupuesto</a>");
        html.append("</div>");

        html.append("<p style='margin-top: 30px; font-size: 14px; color: #666;'>");
        html.append("Para aprobar o rechazar este presupuesto, haga clic en uno de los botones anteriores. ");
        html.append("Los enlaces son válidos por 7 días y solo pueden usarse una vez.");
        html.append("</p>");

        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Este es un email automático generado por SIGRET</p>");
        html.append("<p>Si tiene alguna consulta, por favor responda a este email</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}