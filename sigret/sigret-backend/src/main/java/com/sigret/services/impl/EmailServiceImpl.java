package com.sigret.services.impl;

import com.sigret.services.EmailService;
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

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

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
        // TODO: Implementar envío de presupuesto
        // Este método legacy necesita ser implementado o migrado a un servicio dedicado de presupuestos
        log.warn("Método enviarPresupuestoACliente no implementado para presupuesto ID: {}", presupuestoId);
        throw new UnsupportedOperationException("Método no implementado. Use el servicio de presupuestos.");
    }
}
