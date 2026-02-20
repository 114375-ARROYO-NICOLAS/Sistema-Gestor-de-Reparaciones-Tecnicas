package com.sigret.services.impl;

import com.sigret.entities.Cliente;
import com.sigret.entities.Presupuesto;
import com.sigret.entities.Servicio;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.repositories.ServicioRepository;
import com.sigret.services.EmailService;
import com.sigret.services.PdfService;
import com.sigret.services.PresupuestoTokenService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private PresupuestoTokenService tokenService;

    @Autowired
    private PdfService pdfService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CID_LOGO = "logo";

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${sigret.frontend.url}")
    private String frontendUrl;

    private static final String FROM_NAME = "ArroyoElectromecanica®";

    // ── API pública ──────────────────────────────────────────────────

    @Override
    public void enviarEmail(String destinatario, String asunto, String mensaje) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail, FROM_NAME));
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje);
            mailSender.send(mimeMessage);
            log.info("Email enviado exitosamente a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email a {}", destinatario, e);
            throw new RuntimeException("Error al enviar email", e);
        }
    }

    @Override
    public void enviarEmailConAdjunto(String destinatario, String asunto, String mensaje,
                                      byte[] adjunto, String nombreAdjunto) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail, FROM_NAME));
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje);
            helper.addAttachment(nombreAdjunto, new ByteArrayResource(adjunto));
            mailSender.send(mimeMessage);
            log.info("Email con adjunto enviado exitosamente a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email con adjunto a {}", destinatario, e);
            throw new RuntimeException("Error al enviar email con adjunto", e);
        }
    }

    @Override
    public void enviarPresupuestoACliente(Long presupuestoId, Boolean mostrarOriginal,
                                          Boolean mostrarAlternativo, String mensajeAdicional) {
        try {
            Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                    .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));
            Servicio servicio = presupuesto.getServicio();
            Cliente cliente = servicio.getCliente();

            String emailCliente = cliente.getPrimerEmail();
            if (emailCliente == null || emailCliente.trim().isEmpty())
                throw new RuntimeException("El cliente no tiene email registrado");
            if (presupuesto.getFechaVencimiento() == null)
                throw new RuntimeException("El presupuesto debe tener una fecha de vencimiento antes de ser enviado");
            if (presupuesto.getFechaVencimiento().isBefore(java.time.LocalDate.now()))
                throw new RuntimeException("La fecha de vencimiento del presupuesto no puede ser anterior a hoy");

            tokenService.invalidarTokensAnteriores(presupuestoId);

            String tipoPrecioAprobar = null;
            boolean soloOriginal    = (mostrarOriginal    != null && mostrarOriginal)    && (mostrarAlternativo == null || !mostrarAlternativo);
            boolean soloAlternativo = (mostrarAlternativo != null && mostrarAlternativo) && (mostrarOriginal    == null || !mostrarOriginal);
            if (soloOriginal)    tipoPrecioAprobar = "ORIGINAL";
            if (soloAlternativo) tipoPrecioAprobar = "ALTERNATIVO";

            // Un único token sirve para aprobar o rechazar (la acción la elige el cliente en la página)
            String token = tokenService.generarToken(presupuestoId, "APROBAR", tipoPrecioAprobar);

            String urlRevisar = frontendUrl + "/p/" + token;

            byte[] pdfBytes  = pdfService.generarPdfPresupuesto(presupuestoId, mostrarOriginal, mostrarAlternativo);
            byte[] logoBytes = cargarLogoBytes();

            String asunto = "Presupuesto " + presupuesto.getNumeroPresupuesto() + " - Arroyo Electromecánica";
            String html   = construirEmailPresupuesto(presupuesto, cliente, servicio,
                    urlRevisar, mensajeAdicional, logoBytes != null);

            enviarHtmlConLogoYAdjunto(emailCliente, asunto, html, logoBytes,
                    pdfBytes, "presupuesto-" + presupuesto.getNumeroPresupuesto() + ".pdf");

            log.info("Presupuesto {} enviado exitosamente a {}", presupuesto.getNumeroPresupuesto(), emailCliente);

        } catch (Exception e) {
            log.error("Error al enviar presupuesto por email", e);
            throw new RuntimeException("Error al enviar presupuesto por email: " + e.getMessage(), e);
        }
    }

    @Override
    public void enviarPdfPorEmail(Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        String email = servicio.getCliente().getPrimerEmail();
        if (email == null || email.isEmpty())
            throw new RuntimeException("El cliente no tiene un email registrado");

        byte[] pdfBytes  = pdfService.generarPdfServicio(servicioId);
        byte[] logoBytes = cargarLogoBytes();
        String asunto    = "Comprobante de Servicio Nº " + servicio.getNumeroServicio() + " - Arroyo Electromecánica";
        String html      = construirEmailServicio(servicio, logoBytes != null);

        enviarHtmlConLogoYAdjunto(email, asunto, html, logoBytes,
                pdfBytes, "comprobante-servicio-" + servicio.getNumeroServicio() + ".pdf");

        log.info("PDF del servicio {} enviado por email a {}", servicioId, email);
    }

    @Override
    public void enviarPdfFinalPorEmail(Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        String email = servicio.getCliente().getPrimerEmail();
        if (email == null || email.isEmpty())
            throw new RuntimeException("El cliente no tiene un email registrado");

        byte[] pdfBytes  = pdfService.generarPdfFinal(servicioId);
        byte[] logoBytes = cargarLogoBytes();
        String asunto    = "Comprobante Final de Servicio Nº " + servicio.getNumeroServicio() + " - Arroyo Electromecánica";
        String html      = construirEmailServicioFinal(servicio, logoBytes != null);

        enviarHtmlConLogoYAdjunto(email, asunto, html, logoBytes,
                pdfBytes, "comprobante-final-" + servicio.getNumeroServicio() + ".pdf");

        log.info("PDF final del servicio {} enviado por email a {}", servicioId, email);
    }

    // ── Envío interno ─────────────────────────────────────────────────

    /**
     * Envía un email HTML con el logo embebido como CID (funciona en Gmail/Outlook)
     * y un PDF como adjunto.
     */
    private void enviarHtmlConLogoYAdjunto(String destinatario, String asunto, String html,
                                            byte[] logoBytes, byte[] pdfBytes, String nombrePdf) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail, FROM_NAME));
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);
            if (logoBytes != null) {
                helper.addInline(CID_LOGO, new ByteArrayResource(logoBytes), "image/png");
            }
            helper.addAttachment(nombrePdf, new ByteArrayResource(pdfBytes));
            mailSender.send(mimeMessage);
            log.info("Email HTML enviado exitosamente a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email HTML a {}", destinatario, e);
            throw new RuntimeException("Error al enviar email", e);
        }
    }

    // ── Constructores de HTML ────────────────────────────────────────

    private String construirEmailPresupuesto(Presupuesto presupuesto, Cliente cliente, Servicio servicio,
                                              String urlRevisar,
                                              String mensajeAdicional, boolean tieneLogo) {
        String equipo    = servicio.getEquipo().getDescripcionCompleta();
        String fechaVenc = presupuesto.getFechaVencimiento() != null
                ? presupuesto.getFechaVencimiento().format(DATE_FORMATTER) : null;

        String btnBase    = "display:inline-block;padding:13px 36px;text-decoration:none;border-radius:6px;"
                          + "font-weight:bold;font-size:15px;font-family:Arial,sans-serif;text-align:center;";
        String btnRevisar = btnBase + "background-color:#1c6091;color:#ffffff;";

        StringBuilder html = new StringBuilder(htmlHead());
        html.append(htmlHeader(tieneLogo, "Presupuesto " + presupuesto.getNumeroPresupuesto()));

        html.append("<div class='content'>");
        html.append("<p>Estimado/a <strong>").append(cliente.getNombreCompleto()).append("</strong>,</p>");
        html.append("<p>Le informamos que su presupuesto está listo. Adjuntamos el PDF con el detalle completo de precios.</p>");

        if (mensajeAdicional != null && !mensajeAdicional.trim().isEmpty()) {
            html.append("<div class='info-box'><p><strong>Mensaje del taller:</strong></p><p>")
                .append(mensajeAdicional).append("</p></div>");
        }

        html.append("<div class='info-box'>")
            .append("<p><strong>Servicio Nº:</strong> ").append(servicio.getNumeroServicio()).append("</p>")
            .append("<p><strong>Equipo:</strong> ").append(equipo).append("</p>");
        if (presupuesto.getDiagnostico() != null && !presupuesto.getDiagnostico().isEmpty())
            html.append("<p><strong>Diagnóstico:</strong> ").append(presupuesto.getDiagnostico()).append("</p>");
        if (fechaVenc != null)
            html.append("<p><strong>Válido hasta:</strong> ").append(fechaVenc).append("</p>");
        html.append("</div>");

        html.append("<p style='color:#555;font-size:14px;'>Por favor revise el PDF adjunto y responda a la brevedad haciendo clic en el siguiente botón:</p>");

        // 1 botón genérico "Revisar Presupuesto"
        html.append("<table width='100%' cellpadding='0' cellspacing='0' style='margin:24px 0;'>")
            .append("<tr><td align='center'>")
            .append("<a href='").append(urlRevisar).append("' style='").append(btnRevisar).append("'>&#128196;&nbsp; Revisar Presupuesto</a>")
            .append("</td></tr></table>");

        html.append("<p style='font-size:12px;color:#888;'>Si tiene alguna consulta, contáctenos directamente. No responda a este correo automático.</p>");
        html.append("</div>");
        html.append(htmlFooter());
        html.append("</body></html>");
        return html.toString();
    }

    private String construirEmailServicio(Servicio servicio, boolean tieneLogo) {
        Cliente cliente  = servicio.getCliente();
        String equipo    = servicio.getEquipo().getDescripcionCompleta();
        String fechaRec  = servicio.getFechaRecepcion().format(DATE_FORMATTER);

        StringBuilder html = new StringBuilder(htmlHead());
        html.append(htmlHeader(tieneLogo, "Comprobante de Servicio Nº " + servicio.getNumeroServicio()));

        html.append("<div class='content'>");
        html.append("<p>Estimado/a <strong>").append(cliente.getNombreCompleto()).append("</strong>,</p>");
        html.append("<p>Su equipo ha ingresado a nuestro taller. Adjuntamos el comprobante de recepción correspondiente.</p>");

        html.append("<div class='info-box'>")
            .append("<p><strong>Servicio Nº:</strong> ").append(servicio.getNumeroServicio()).append("</p>")
            .append("<p><strong>Equipo:</strong> ").append(equipo).append("</p>")
            .append("<p><strong>Fecha de recepción:</strong> ").append(fechaRec).append("</p>");
        if (servicio.getFallaReportada() != null && !servicio.getFallaReportada().isEmpty())
            html.append("<p><strong>Falla reportada:</strong> ").append(servicio.getFallaReportada()).append("</p>");
        html.append("</div>");

        html.append("<p style='color:#555;font-size:14px;'>En cuanto tengamos el diagnóstico y presupuesto listo, nos comunicaremos con usted.</p>");
        html.append("<p style='font-size:12px;color:#888;'>Si tiene alguna consulta, contáctenos directamente. No responda a este correo automático.</p>");
        html.append("</div>");
        html.append(htmlFooter());
        html.append("</body></html>");
        return html.toString();
    }

    private String construirEmailServicioFinal(Servicio servicio, boolean tieneLogo) {
        Cliente cliente      = servicio.getCliente();
        String equipo        = servicio.getEquipo().getDescripcionCompleta();
        String fechaRec      = servicio.getFechaRecepcion().format(DATE_FORMATTER);
        String fechaDevol    = servicio.getFechaDevolucionReal() != null
                ? servicio.getFechaDevolucionReal().format(DATE_FORMATTER) : "N/A";

        StringBuilder html = new StringBuilder(htmlHead());
        html.append(htmlHeader(tieneLogo, "Comprobante Final de Servicio Nº " + servicio.getNumeroServicio()));

        html.append("<div class='content'>");
        html.append("<p>Estimado/a <strong>").append(cliente.getNombreCompleto()).append("</strong>,</p>");
        html.append("<p>Su equipo está listo para ser retirado. Adjuntamos el comprobante final del servicio técnico realizado.</p>");

        html.append("<div class='info-box'>")
            .append("<p><strong>Servicio Nº:</strong> ").append(servicio.getNumeroServicio()).append("</p>")
            .append("<p><strong>Equipo:</strong> ").append(equipo).append("</p>")
            .append("<p><strong>Fecha de recepción:</strong> ").append(fechaRec).append("</p>")
            .append("<p><strong>Fecha de devolución:</strong> ").append(fechaDevol).append("</p>")
            .append("</div>");

        html.append("<p style='color:#555;font-size:14px;'>Puede pasar a retirar su equipo en el horario de atención del taller.</p>");
        html.append("<p style='font-size:12px;color:#888;'>¡Gracias por confiar en Arroyo Electromecánica!</p>");
        html.append("</div>");
        html.append(htmlFooter());
        html.append("</body></html>");
        return html.toString();
    }

    // ── Fragmentos HTML compartidos ──────────────────────────────────

    private String htmlHead() {
        return "<!DOCTYPE html><html lang='es'><head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
               "<style>" +
               "body{font-family:Arial,sans-serif;line-height:1.6;color:#333;max-width:600px;margin:0 auto;padding:0;}" +
               ".content{background:#f8f9fa;padding:24px;}" +
               ".info-box{background:white;padding:15px;margin:16px 0;border-radius:5px;border-left:4px solid #1c6091;}" +
               ".footer{background:#0d2d4c;color:white;padding:15px;text-align:center;font-size:12px;border-radius:0 0 5px 5px;}" +
               "p{margin:0 0 10px;}" +
               "</style></head><body>";
    }

    /** Header celeste con logo CID (compatible Gmail/Outlook) o texto de respaldo */
    private String htmlHeader(boolean tieneLogo, String subtitulo) {
        StringBuilder sb = new StringBuilder();
        // Franja brand en la parte superior (email-safe: div sólido)
        sb.append("<div style='background:#1c6091;height:6px;border-radius:5px 5px 0 0;'></div>");
        // Cuerpo del header en celeste claro
        sb.append("<div style='background:#e8f4fd;padding:22px 20px 18px;text-align:center;border-bottom:1px solid #c3ddf0;'>");
        if (tieneLogo) {
            sb.append("<img src='cid:").append(CID_LOGO)
              .append("' alt='Arroyo Electromecánica' style='max-height:60px;width:auto;display:block;margin:0 auto;'/>");
            sb.append("<p style='color:#1c4a6e;margin:10px 0 0;font-size:13px;font-weight:600;letter-spacing:0.3px;'>")
              .append(subtitulo).append("</p>");
        } else {
            sb.append("<h1 style='margin:0;color:#1c3550;font-size:20px;'>Arroyo Electromecánica</h1>");
            sb.append("<p style='color:#1c4a6e;margin:6px 0 0;font-size:13px;'>").append(subtitulo).append("</p>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String htmlFooter() {
        return "<div class='footer'>" +
               "<p style='margin:0 0 4px;font-weight:bold;'>Arroyo Electromecánica</p>" +
               "<p style='margin:0;'>Este es un correo automático, por favor no responda a este mensaje.</p>" +
               "</div>";
    }

    private byte[] cargarLogoBytes() {
        try {
            InputStream logoStream = getClass().getClassLoader()
                    .getResourceAsStream("logos/logo-horizontal-original.png");
            if (logoStream != null) return logoStream.readAllBytes();
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo para el email", e);
        }
        return null;
    }
}
