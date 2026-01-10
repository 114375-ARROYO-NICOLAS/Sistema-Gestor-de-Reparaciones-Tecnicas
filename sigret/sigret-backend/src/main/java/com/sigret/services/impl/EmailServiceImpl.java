package com.sigret.services.impl;

import com.sigret.entities.Cliente;
import com.sigret.entities.Presupuesto;
import com.sigret.entities.Servicio;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.services.EmailService;
import com.sigret.services.PresupuestoTokenService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private PresupuestoTokenService tokenService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${sigret.frontend.url}")
    private String frontendUrl;

    private static final String FROM_NAME = "ArroyoElectromecanica®";

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
    public void enviarEmailConAdjunto(String destinatario, String asunto, String mensaje, byte[] adjunto, String nombreAdjunto) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, FROM_NAME));
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje);

            // Agregar adjunto
            ByteArrayResource adjuntoResource = new ByteArrayResource(adjunto);
            helper.addAttachment(nombreAdjunto, adjuntoResource);

            mailSender.send(mimeMessage);
            log.info("Email con adjunto enviado exitosamente a {}", destinatario);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error al enviar email con adjunto a {}", destinatario, e);
            throw new RuntimeException("Error al enviar email con adjunto", e);
        }
    }

    @Override
    public void enviarPresupuestoACliente(Long presupuestoId, Boolean mostrarOriginal, Boolean mostrarAlternativo, String mensajeAdicional) {
        try {
            // Obtener el presupuesto
            Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                    .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

            Servicio servicio = presupuesto.getServicio();
            Cliente cliente = servicio.getCliente();

            // Validar que el cliente tenga email
            String emailCliente = cliente.getPrimerEmail();
            if (emailCliente == null || emailCliente.trim().isEmpty()) {
                throw new RuntimeException("El cliente no tiene email registrado");
            }

            // Invalidar tokens anteriores
            tokenService.invalidarTokensAnteriores(presupuestoId);

            // Generar tokens para aprobación y rechazo
            String tokenAprobarOriginal = null;
            String tokenAprobarAlternativo = null;

            if (mostrarOriginal != null && mostrarOriginal) {
                tokenAprobarOriginal = tokenService.generarToken(presupuestoId, "APROBAR", "ORIGINAL");
            }

            if (mostrarAlternativo != null && mostrarAlternativo) {
                tokenAprobarAlternativo = tokenService.generarToken(presupuestoId, "APROBAR", "ALTERNATIVO");
            }

            String tokenRechazar = tokenService.generarToken(presupuestoId, "RECHAZAR", null);

            // Construir URLs
            String urlBasePublica = frontendUrl + "/p";
            String urlAprobarOriginal = tokenAprobarOriginal != null
                    ? urlBasePublica + "/" + tokenAprobarOriginal + "/aprobar"
                    : null;
            String urlAprobarAlternativo = tokenAprobarAlternativo != null
                    ? urlBasePublica + "/" + tokenAprobarAlternativo + "/aprobar"
                    : null;
            String urlRechazar = urlBasePublica + "/" + tokenRechazar + "/rechazar";

            // Construir el email HTML
            String asunto = "Presupuesto " + presupuesto.getNumeroPresupuesto() + " - ArroyoElectromecanica®";
            String mensaje = construirEmailPresupuesto(
                    presupuesto,
                    cliente,
                    servicio,
                    mostrarOriginal,
                    mostrarAlternativo,
                    urlAprobarOriginal,
                    urlAprobarAlternativo,
                    urlRechazar,
                    mensajeAdicional
            );

            // Enviar email HTML
            enviarEmailHtml(emailCliente, asunto, mensaje);

            log.info("Presupuesto {} enviado exitosamente a {}", presupuesto.getNumeroPresupuesto(), emailCliente);

        } catch (Exception e) {
            log.error("Error al enviar presupuesto por email", e);
            throw new RuntimeException("Error al enviar presupuesto por email: " + e.getMessage(), e);
        }
    }

    private void enviarEmailHtml(String destinatario, String asunto, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, FROM_NAME));
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(mimeMessage);
            log.info("Email HTML enviado exitosamente a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar email HTML a {}", destinatario, e);
            throw new RuntimeException("Error al enviar email HTML", e);
        }
    }

    private String construirEmailPresupuesto(
            Presupuesto presupuesto,
            Cliente cliente,
            Servicio servicio,
            Boolean mostrarOriginal,
            Boolean mostrarAlternativo,
            String urlAprobarOriginal,
            String urlAprobarAlternativo,
            String urlRechazar,
            String mensajeAdicional) {

        NumberFormat formatoPrecio = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='es'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }");
        html.append(".content { background-color: #f8f9fa; padding: 20px; }");
        html.append(".info-box { background-color: white; padding: 15px; margin: 10px 0; border-radius: 5px; border-left: 4px solid #3498db; }");
        html.append(".precio-box { background-color: #e8f5e9; padding: 15px; margin: 15px 0; border-radius: 5px; border: 2px solid #4caf50; }");
        html.append(".precio-alternativo { background-color: #fff3e0; border: 2px solid #ff9800; }");
        html.append(".botones { text-align: center; margin: 20px 0; }");
        html.append(".boton { display: inline-block; padding: 12px 30px; margin: 10px 5px; text-decoration: none; border-radius: 5px; font-weight: bold; }");
        html.append(".boton-aprobar { background-color: #4caf50; color: white; }");
        html.append(".boton-rechazar { background-color: #f44336; color: white; }");
        html.append(".footer { background-color: #34495e; color: white; padding: 15px; text-align: center; font-size: 12px; border-radius: 0 0 5px 5px; }");
        html.append("h2 { color: #2c3e50; margin-top: 0; }");
        html.append("h3 { color: #34495e; }");
        html.append(".detalle { margin: 10px 0; padding: 8px; background-color: #f5f5f5; border-radius: 3px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1 style='margin: 0;'>ArroyoElectromecanica®</h1>");
        html.append("<p style='margin: 5px 0;'>Presupuesto ").append(presupuesto.getNumeroPresupuesto()).append("</p>");
        html.append("</div>");

        // Contenido
        html.append("<div class='content'>");

        // Saludo
        html.append("<p>Estimado/a <strong>").append(cliente.getNombreCompleto()).append("</strong>,</p>");
        html.append("<p>Le enviamos el presupuesto solicitado para la reparación de su equipo.</p>");

        // Mensaje adicional
        if (mensajeAdicional != null && !mensajeAdicional.trim().isEmpty()) {
            html.append("<div class='info-box'>");
            html.append("<p><strong>Mensaje:</strong></p>");
            html.append("<p>").append(mensajeAdicional).append("</p>");
            html.append("</div>");
        }

        // Información del servicio
        html.append("<div class='info-box'>");
        html.append("<h3>Detalles del Servicio</h3>");
        html.append("<p><strong>Número de Servicio:</strong> ").append(servicio.getNumeroServicio()).append("</p>");
        html.append("<p><strong>Equipo:</strong> ");
        if (servicio.getEquipo().getTipoEquipo() != null) {
            html.append(servicio.getEquipo().getTipoEquipo().getDescripcion());
        }
        if (servicio.getEquipo().getMarca() != null) {
            html.append(" ").append(servicio.getEquipo().getMarca().getDescripcion());
        }
        if (servicio.getEquipo().getModelo() != null) {
            html.append(" ").append(servicio.getEquipo().getModelo().getDescripcion());
        }
        html.append("</p>");
        if (presupuesto.getDiagnostico() != null) {
            html.append("<p><strong>Diagnóstico:</strong> ").append(presupuesto.getDiagnostico()).append("</p>");
        }
        html.append("</div>");

        // Precios Original
        if (mostrarOriginal != null && mostrarOriginal) {
            html.append("<div class='precio-box'>");
            html.append("<h3 style='color: #4caf50; margin-top: 0;'>Presupuesto con Repuestos Originales</h3>");
            html.append("<p><strong>Repuestos:</strong> ").append(formatoPrecio.format(presupuesto.getMontoRepuestosOriginal())).append("</p>");
            html.append("<p><strong>Mano de Obra:</strong> ").append(formatoPrecio.format(presupuesto.getManoObra())).append("</p>");
            html.append("<p style='font-size: 18px; font-weight: bold; color: #2c3e50;'>");
            html.append("<strong>TOTAL:</strong> ").append(formatoPrecio.format(presupuesto.getMontoTotalOriginal()));
            html.append("</p>");
            html.append("</div>");
        }

        // Precios Alternativo
        if (mostrarAlternativo != null && mostrarAlternativo && presupuesto.getMontoRepuestosAlternativo() != null) {
            html.append("<div class='precio-box precio-alternativo'>");
            html.append("<h3 style='color: #ff9800; margin-top: 0;'>Presupuesto con Repuestos Alternativos</h3>");
            html.append("<p><strong>Repuestos:</strong> ").append(formatoPrecio.format(presupuesto.getMontoRepuestosAlternativo())).append("</p>");
            html.append("<p><strong>Mano de Obra:</strong> ").append(formatoPrecio.format(presupuesto.getManoObra())).append("</p>");
            html.append("<p style='font-size: 18px; font-weight: bold; color: #2c3e50;'>");
            html.append("<strong>TOTAL:</strong> ").append(formatoPrecio.format(presupuesto.getMontoTotalAlternativo()));
            html.append("</p>");
            html.append("</div>");
        }

        // Botones de acción
        html.append("<div class='botones'>");
        html.append("<h3>¿Desea aprobar el presupuesto?</h3>");

        if (urlAprobarOriginal != null) {
            html.append("<a href='").append(urlAprobarOriginal).append("' class='boton boton-aprobar'>");
            html.append("✓ Aprobar Presupuesto Original");
            html.append("</a>");
        }

        if (urlAprobarAlternativo != null) {
            html.append("<a href='").append(urlAprobarAlternativo).append("' class='boton boton-aprobar'>");
            html.append("✓ Aprobar Presupuesto Alternativo");
            html.append("</a>");
        }

        html.append("<br>");
        html.append("<a href='").append(urlRechazar).append("' class='boton boton-rechazar'>");
        html.append("✗ Rechazar Presupuesto");
        html.append("</a>");
        html.append("</div>");

        // Información adicional
        html.append("<div class='info-box'>");
        html.append("<p><strong>Importante:</strong></p>");
        html.append("<ul>");
        html.append("<li>Este presupuesto tiene una validez de 7 días.</li>");
        html.append("<li>Una vez aprobado, procederemos con la reparación.</li>");
        html.append("<li>Si tiene alguna consulta, no dude en contactarnos.</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p><strong>ArroyoElectromecanica®</strong></p>");
        html.append("<p>Este es un correo automático, por favor no responda a este mensaje.</p>");
        html.append("<p>Para cualquier consulta, contáctenos directamente.</p>");
        html.append("</div>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
