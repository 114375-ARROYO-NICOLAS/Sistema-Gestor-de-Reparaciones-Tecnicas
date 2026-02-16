package com.sigret.services.impl;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sigret.entities.*;
import com.sigret.enums.EstadoOrdenTrabajo;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.repositories.ServicioRepository;
import com.sigret.services.EmailService;
import com.sigret.services.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@Slf4j
public class PdfServiceImpl implements PdfService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generarPdfServicio(Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Agregar contenido al PDF
            agregarEncabezado(document, servicio);
            agregarInformacionCliente(document, servicio);
            agregarInformacionEquipo(document, servicio);
            agregarDetallesServicio(document, servicio);
            agregarComponentes(document, servicio);
            agregarFirma(document, servicio);
            agregarPiePagina(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF para servicio {}", servicioId, e);
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    @Override
    public void enviarPdfPorEmail(Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        // Obtener email del cliente
        String email = servicio.getCliente().getPrimerEmail();
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("El cliente no tiene un email registrado");
        }

        // Generar PDF
        byte[] pdfBytes = generarPdfServicio(servicioId);

        // Enviar email
        String asunto = "Comprobante de Servicio - " + servicio.getNumeroServicio();
        String mensaje = String.format(
                "Estimado/a %s,\n\n" +
                        "Adjuntamos el comprobante de recepción del servicio técnico número %s.\n\n" +
                        "Equipo: %s\n" +
                        "Fecha de recepción: %s\n\n" +
                        "Gracias por confiar en nuestros servicios.\n\n" +
                        "Saludos cordiales,\n" +
                        "Arroyo Electromecánica",
                servicio.getCliente().getNombreCompleto(),
                servicio.getNumeroServicio(),
                servicio.getEquipo().getDescripcionCompleta(),
                servicio.getFechaRecepcion().format(DATE_FORMATTER)
        );

        emailService.enviarEmailConAdjunto(
                email,
                asunto,
                mensaje,
                pdfBytes,
                "comprobante-servicio-" + servicio.getNumeroServicio() + ".pdf"
        );

        log.info("PDF del servicio {} enviado por email a {}", servicioId, email);
    }

    private void agregarEncabezado(Document document, Servicio servicio) {
        // Nombre de la empresa
        Paragraph empresa = new Paragraph("Arroyo Electromecánica")
                .setFontSize(12)
                .setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(empresa);

        // Título principal
        Paragraph titulo = new Paragraph("COMPROBANTE DE SERVICIO TÉCNICO")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(titulo);

        // Número de servicio
        Paragraph numeroServicio = new Paragraph("Nº " + servicio.getNumeroServicio())
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(numeroServicio);

        // Línea separadora
        SolidLine line = new SolidLine(1f);
        line.setColor(ColorConstants.GRAY);
        document.add(new LineSeparator(line));
        document.add(new Paragraph("\n"));
    }

    private void agregarInformacionCliente(Document document, Servicio servicio) {
        Cliente cliente = servicio.getCliente();
        Persona persona = cliente.getPersona();

        Paragraph seccion = new Paragraph("INFORMACIÓN DEL CLIENTE")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);
        document.add(seccion);

        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        table.setWidth(UnitValue.createPercentValue(100));

        agregarFilaTabla(table, "Nombre:", cliente.getNombreCompleto());
        agregarFilaTabla(table, "Documento:", persona.getTipoDocumento().getDescripcion() + " " + persona.getDocumento());

        // Agregar email si existe
        String email = cliente.getPrimerEmail();
        if (email != null && !email.isEmpty()) {
            agregarFilaTabla(table, "Email:", email);
        }

        // Agregar teléfono si existe
        String telefono = cliente.getPrimerTelefono();
        if (telefono != null && !telefono.isEmpty()) {
            agregarFilaTabla(table, "Teléfono:", telefono);
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void agregarInformacionEquipo(Document document, Servicio servicio) {
        Equipo equipo = servicio.getEquipo();

        Paragraph seccion = new Paragraph("INFORMACIÓN DEL EQUIPO")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);
        document.add(seccion);

        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        table.setWidth(UnitValue.createPercentValue(100));

        agregarFilaTabla(table, "Tipo:", equipo.getTipoEquipo().getDescripcion());
        agregarFilaTabla(table, "Marca:", equipo.getMarca().getDescripcion());

        if (equipo.getModelo() != null) {
            agregarFilaTabla(table, "Modelo:", equipo.getModelo().getDescripcion());
        }

        if (equipo.getNumeroSerie() != null && !equipo.getNumeroSerie().isEmpty()) {
            agregarFilaTabla(table, "N° de Serie:", equipo.getNumeroSerie());
        }

        if (equipo.getColor() != null && !equipo.getColor().isEmpty()) {
            agregarFilaTabla(table, "Color:", equipo.getColor());
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void agregarDetallesServicio(Document document, Servicio servicio) {
        Paragraph seccion = new Paragraph("DETALLES DEL SERVICIO")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);
        document.add(seccion);

        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        table.setWidth(UnitValue.createPercentValue(100));

        agregarFilaTabla(table, "Fecha de Recepción:", servicio.getFechaRecepcion().format(DATE_FORMATTER));

        if (servicio.getFechaDevolucionPrevista() != null) {
            agregarFilaTabla(table, "Fecha Devolución Prevista:", servicio.getFechaDevolucionPrevista().format(DATE_FORMATTER));
        }

        agregarFilaTabla(table, "Tipo de Ingreso:", servicio.getTipoIngreso().getDescripcion());
        agregarFilaTabla(table, "Empleado de Recepción:", servicio.getEmpleadoRecepcion().getPersona().getNombreCompleto());

        if (servicio.getEsGarantia()) {
            agregarFilaTabla(table, "Es Garantía:", "SÍ");
            if (servicio.getServicioGarantia() != null) {
                agregarFilaTabla(table, "Servicio Original:", servicio.getServicioGarantia().getNumeroServicio());
            }
        }

        if (servicio.getFallaReportada() != null && !servicio.getFallaReportada().isEmpty()) {
            agregarFilaTabla(table, "Falla Reportada:", servicio.getFallaReportada());
        }

        if (servicio.getObservaciones() != null && !servicio.getObservaciones().isEmpty()) {
            agregarFilaTabla(table, "Observaciones:", servicio.getObservaciones());
        }

        if (servicio.getAbonaVisita()) {
            agregarFilaTabla(table, "Abona Visita:", "SÍ");
            agregarFilaTabla(table, "Monto Visita:", "$" + servicio.getMontoVisita().toString());
            agregarFilaTabla(table, "Monto Pagado:", "$" + servicio.getMontoPagado().toString());
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void agregarComponentes(Document document, Servicio servicio) {
        if (servicio.getDetalleServicios() == null || servicio.getDetalleServicios().isEmpty()) {
            return;
        }

        Paragraph seccion = new Paragraph("COMPONENTES DEL EQUIPO")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);
        document.add(seccion);

        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 15, 45}));
        table.setWidth(UnitValue.createPercentValue(100));

        // Encabezados
        Cell headerComponente = new Cell().add(new Paragraph("Componente").setBold())
                .setBackgroundColor(new DeviceRgb(230, 230, 230))
                .setTextAlignment(TextAlignment.CENTER);
        Cell headerPresente = new Cell().add(new Paragraph("Estado").setBold())
                .setBackgroundColor(new DeviceRgb(230, 230, 230))
                .setTextAlignment(TextAlignment.CENTER);
        Cell headerComentario = new Cell().add(new Paragraph("Comentario").setBold())
                .setBackgroundColor(new DeviceRgb(230, 230, 230))
                .setTextAlignment(TextAlignment.CENTER);

        table.addCell(headerComponente);
        table.addCell(headerPresente);
        table.addCell(headerComentario);

        // Datos
        for (DetalleServicio detalle : servicio.getDetalleServicios()) {
            table.addCell(new Cell().add(new Paragraph(detalle.getComponente())));
            table.addCell(new Cell().add(new Paragraph(detalle.getPresente() ? "Presente" : "No Presente"))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(detalle.getComentario() != null ? detalle.getComentario() : "-")));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void agregarFirma(Document document, Servicio servicio) {
        if (servicio.getFirmaIngreso() == null || servicio.getFirmaIngreso().isEmpty()) {
            return;
        }

        Paragraph seccion = new Paragraph("FIRMA DEL CLIENTE")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);
        document.add(seccion);

        try {
            // Decodificar la imagen base64
            byte[] imageBytes = Base64.getDecoder().decode(servicio.getFirmaIngreso());
            Image firma = new Image(ImageDataFactory.create(imageBytes));

            // Ajustar tamaño de la firma
            firma.setWidth(200);
            firma.setHeight(100);

            document.add(firma);
        } catch (Exception e) {
            log.error("Error al agregar firma al PDF", e);
            document.add(new Paragraph("Error al cargar firma"));
        }

        document.add(new Paragraph("\n"));
    }

    private void agregarPiePagina(Document document) {
        document.add(new Paragraph("\n"));
        SolidLine footerLine = new SolidLine(0.5f);
        footerLine.setColor(ColorConstants.GRAY);
        document.add(new LineSeparator(footerLine));

        Paragraph pie = new Paragraph("TÉRMINOS Y CONDICIONES")
                .setFontSize(8)
                .setBold()
                .setMarginTop(10);
        document.add(pie);

        Paragraph terminos = new Paragraph(
                "El cliente acepta que el equipo será revisado y diagnosticado por personal técnico calificado. " +
                        "La empresa se compromete a notificar al cliente sobre el diagnóstico y presupuesto correspondiente. " +
                        "Los equipos no retirados en un plazo de 90 días podrán ser dados de baja según lo establecido en nuestros términos de servicio. " +
                        "Este documento constituye un comprobante de recepción del equipo en las condiciones descritas."
        )
                .setFontSize(7)
                .setTextAlignment(TextAlignment.JUSTIFIED);
        document.add(terminos);
    }

    private void agregarFilaTabla(Table table, String etiqueta, String valor) {
        Cell cellEtiqueta = new Cell().add(new Paragraph(etiqueta).setBold());
        Cell cellValor = new Cell().add(new Paragraph(valor != null ? valor : "-"));
        table.addCell(cellEtiqueta);
        table.addCell(cellValor);
    }

    // ==================== PDF FINAL ====================

    @Override
    public byte[] generarPdfFinal(Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            agregarEncabezadoFinal(document, servicio);
            agregarInformacionCliente(document, servicio);
            agregarInformacionEquipo(document, servicio);
            agregarDetallesServicio(document, servicio);
            agregarPresupuestoAprobado(document, servicio);
            agregarOrdenTrabajo(document, servicio);
            agregarConformidad(document, servicio);
            agregarPiePagina(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF final para servicio {}", servicioId, e);
            throw new RuntimeException("Error al generar PDF final", e);
        }
    }

    @Override
    public void enviarPdfFinalPorEmail(Long servicioId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        String email = servicio.getCliente().getPrimerEmail();
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("El cliente no tiene un email registrado");
        }

        byte[] pdfBytes = generarPdfFinal(servicioId);

        String asunto = "Comprobante Final de Servicio - " + servicio.getNumeroServicio();
        String mensaje = String.format(
                "Estimado/a %s,\n\n" +
                        "Adjuntamos el comprobante final del servicio técnico número %s.\n\n" +
                        "Equipo: %s\n" +
                        "Fecha de recepción: %s\n" +
                        "Fecha de devolución: %s\n\n" +
                        "Gracias por confiar en nuestros servicios.\n\n" +
                        "Saludos cordiales,\n" +
                        "Arroyo Electromecánica",
                servicio.getCliente().getNombreCompleto(),
                servicio.getNumeroServicio(),
                servicio.getEquipo().getDescripcionCompleta(),
                servicio.getFechaRecepcion().format(DATE_FORMATTER),
                servicio.getFechaDevolucionReal() != null ? servicio.getFechaDevolucionReal().format(DATE_FORMATTER) : "N/A"
        );

        emailService.enviarEmailConAdjunto(
                email,
                asunto,
                mensaje,
                pdfBytes,
                "comprobante-final-" + servicio.getNumeroServicio() + ".pdf"
        );

        log.info("PDF final del servicio {} enviado por email a {}", servicioId, email);
    }

    private void agregarEncabezadoFinal(Document document, Servicio servicio) {
        // Nombre de la empresa
        Paragraph empresa = new Paragraph("Arroyo Electromecánica")
                .setFontSize(12)
                .setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(empresa);

        Paragraph titulo = new Paragraph("COMPROBANTE FINAL DE SERVICIO TÉCNICO")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(titulo);

        Paragraph numeroServicio = new Paragraph("Nº " + servicio.getNumeroServicio())
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(numeroServicio);

        Paragraph estado = new Paragraph("Estado: " + servicio.getEstado().getDescripcion())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(estado);

        SolidLine line = new SolidLine(1f);
        line.setColor(ColorConstants.GRAY);
        document.add(new LineSeparator(line));
        document.add(new Paragraph("\n"));
    }

    private void agregarPresupuestoAprobado(Document document, Servicio servicio) {
        // Buscar el presupuesto aprobado o confirmado
        Presupuesto presupuestoAprobado = servicio.getPresupuestos().stream()
                .filter(p -> p.getEstado() == EstadoPresupuesto.APROBADO)
                .findFirst()
                .orElse(null);

        if (presupuestoAprobado == null) {
            return;
        }

        Paragraph seccion = new Paragraph("PRESUPUESTO APROBADO")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);
        document.add(seccion);

        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        table.setWidth(UnitValue.createPercentValue(100));

        agregarFilaTabla(table, "Nº Presupuesto:", presupuestoAprobado.getNumeroPresupuesto());

        if (presupuestoAprobado.getDiagnostico() != null && !presupuestoAprobado.getDiagnostico().isEmpty()) {
            agregarFilaTabla(table, "Diagnóstico:", presupuestoAprobado.getDiagnostico());
        }

        agregarFilaTabla(table, "Mano de Obra:", "$" + presupuestoAprobado.getManoObra().toString());
        agregarFilaTabla(table, "Repuestos:", "$" + presupuestoAprobado.getMontoRepuestosOriginal().toString());
        agregarFilaTabla(table, "Total:", "$" + presupuestoAprobado.getMontoTotalOriginal().toString());

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void agregarOrdenTrabajo(Document document, Servicio servicio) {
        // Buscar la orden de trabajo terminada o la más reciente
        OrdenTrabajo orden = servicio.getOrdenesTrabajo().stream()
                .filter(ot -> ot.getEstado() == EstadoOrdenTrabajo.TERMINADA)
                .findFirst()
                .orElse(null);

        if (orden == null) {
            return;
        }

        Paragraph seccion = new Paragraph("ORDEN DE TRABAJO")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);
        document.add(seccion);

        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
        table.setWidth(UnitValue.createPercentValue(100));

        agregarFilaTabla(table, "Nº Orden:", orden.getNumeroOrdenTrabajo());
        agregarFilaTabla(table, "Técnico:", orden.getEmpleado().getPersona().getNombreCompleto());
        agregarFilaTabla(table, "Estado:", orden.getEstado().getDescripcion());

        if (orden.getFechaComienzo() != null) {
            agregarFilaTabla(table, "Fecha Inicio:", orden.getFechaComienzo().format(DATE_FORMATTER));
        }
        if (orden.getFechaFin() != null) {
            agregarFilaTabla(table, "Fecha Fin:", orden.getFechaFin().format(DATE_FORMATTER));
        }

        if (orden.getObservacionesExtras() != null && !orden.getObservacionesExtras().isEmpty()) {
            agregarFilaTabla(table, "Observaciones:", orden.getObservacionesExtras());
        }

        document.add(table);

        // Detalle de items de la orden
        if (orden.getDetalleOrdenesTrabajo() != null && !orden.getDetalleOrdenesTrabajo().isEmpty()) {
            document.add(new Paragraph("\n"));

            Table tablaItems = new Table(UnitValue.createPercentArray(new float[]{50, 15, 35}));
            tablaItems.setWidth(UnitValue.createPercentValue(100));

            Cell headerItem = new Cell().add(new Paragraph("Item").setBold())
                    .setBackgroundColor(new DeviceRgb(230, 230, 230))
                    .setTextAlignment(TextAlignment.CENTER);
            Cell headerCantidad = new Cell().add(new Paragraph("Cant.").setBold())
                    .setBackgroundColor(new DeviceRgb(230, 230, 230))
                    .setTextAlignment(TextAlignment.CENTER);
            Cell headerComentario = new Cell().add(new Paragraph("Comentario").setBold())
                    .setBackgroundColor(new DeviceRgb(230, 230, 230))
                    .setTextAlignment(TextAlignment.CENTER);

            tablaItems.addCell(headerItem);
            tablaItems.addCell(headerCantidad);
            tablaItems.addCell(headerComentario);

            for (DetalleOrdenTrabajo detalle : orden.getDetalleOrdenesTrabajo()) {
                tablaItems.addCell(new Cell().add(new Paragraph(detalle.getItemDisplay())));
                tablaItems.addCell(new Cell().add(new Paragraph(String.valueOf(detalle.getCantidad())))
                        .setTextAlignment(TextAlignment.CENTER));
                tablaItems.addCell(new Cell().add(new Paragraph(detalle.getComentario() != null ? detalle.getComentario() : "-")));
            }

            document.add(tablaItems);
        }

        document.add(new Paragraph("\n"));
    }

    private void agregarConformidad(Document document, Servicio servicio) {
        Paragraph seccion = new Paragraph("CONFORMIDAD DEL CLIENTE")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);
        document.add(seccion);

        Paragraph textoConformidad = new Paragraph(
                "El cliente declara haber recibido el equipo en condiciones satisfactorias " +
                        "y de conformidad con el servicio realizado."
        )
                .setFontSize(10)
                .setMarginBottom(10);
        document.add(textoConformidad);

        if (servicio.getFechaDevolucionReal() != null) {
            Paragraph fechaDevolucion = new Paragraph("Fecha de devolución: " + servicio.getFechaDevolucionReal().format(DATE_FORMATTER))
                    .setFontSize(10)
                    .setMarginBottom(10);
            document.add(fechaDevolucion);
        }

        // Firma de conformidad
        if (servicio.getFirmaConformidad() != null && !servicio.getFirmaConformidad().isEmpty()) {
            Paragraph firmaLabel = new Paragraph("Firma de Conformidad:")
                    .setFontSize(10)
                    .setBold()
                    .setMarginBottom(5);
            document.add(firmaLabel);

            try {
                byte[] imageBytes = Base64.getDecoder().decode(servicio.getFirmaConformidad());
                Image firma = new Image(ImageDataFactory.create(imageBytes));
                firma.setWidth(200);
                firma.setHeight(100);
                document.add(firma);
            } catch (Exception e) {
                log.error("Error al agregar firma de conformidad al PDF", e);
                document.add(new Paragraph("Error al cargar firma de conformidad"));
            }
        }

        // Firma de ingreso
        if (servicio.getFirmaIngreso() != null && !servicio.getFirmaIngreso().isEmpty()) {
            document.add(new Paragraph("\n"));
            Paragraph firmaIngresoLabel = new Paragraph("Firma de Ingreso:")
                    .setFontSize(10)
                    .setBold()
                    .setMarginBottom(5);
            document.add(firmaIngresoLabel);

            try {
                byte[] imageBytes = Base64.getDecoder().decode(servicio.getFirmaIngreso());
                Image firma = new Image(ImageDataFactory.create(imageBytes));
                firma.setWidth(200);
                firma.setHeight(100);
                document.add(firma);
            } catch (Exception e) {
                log.error("Error al agregar firma de ingreso al PDF final", e);
                document.add(new Paragraph("Error al cargar firma de ingreso"));
            }
        }

        document.add(new Paragraph("\n"));
    }
}
