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
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.InputStream;
import com.sigret.entities.*;
import com.sigret.enums.EstadoOrdenTrabajo;
import com.sigret.enums.EstadoPresupuesto;
import com.sigret.repositories.PresupuestoRepository;
import com.sigret.repositories.ServicioRepository;
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
    private PresupuestoRepository presupuestoRepository;

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
            document.setMargins(20, 36, 20, 36);

            // Agregar contenido al PDF
            agregarEncabezado(document, servicio);
            agregarInformacionClienteYEquipo(document, servicio);
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

    private void agregarEncabezado(Document document, Servicio servicio) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{55, 45}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setBorder(Border.NO_BORDER);
        headerTable.setMarginBottom(6);

        // Celda izquierda: logo
        Cell leftCell = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(0);

        try {
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("logos/logo-horizontal-original.png");
            if (logoStream != null) {
                Image logo = new Image(ImageDataFactory.create(logoStream.readAllBytes()));
                logo.setWidth(170);
                logo.setAutoScaleHeight(true);
                leftCell.add(logo);
            } else {
                leftCell.add(new Paragraph("Arroyo Electromecánica")
                        .setFontSize(12).setBold()
                        .setFontColor(new DeviceRgb(28, 96, 145)));
            }
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo", e);
            leftCell.add(new Paragraph("Arroyo Electromecánica")
                    .setFontSize(12).setBold()
                    .setFontColor(new DeviceRgb(28, 96, 145)));
        }

        // Celda derecha: tipo de comprobante + número
        Cell rightCell = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(0);

        rightCell.add(new Paragraph("COMPROBANTE DE SERVICIO TÉCNICO")
                .setFontSize(8)
                .setBold()
                .setFontColor(new DeviceRgb(80, 80, 80))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(2));

        rightCell.add(new Paragraph("Nº " + servicio.getNumeroServicio())
                .setFontSize(16)
                .setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setTextAlignment(TextAlignment.RIGHT));

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        SolidLine line = new SolidLine(1f);
        line.setColor(new DeviceRgb(28, 96, 145));
        document.add(new LineSeparator(line));
        document.add(new Paragraph("").setMarginBottom(6));
    }

    private void agregarInformacionClienteYEquipo(Document document, Servicio servicio) {
        Cliente cliente = servicio.getCliente();
        Persona persona = cliente.getPersona();
        Equipo equipo = servicio.getEquipo();

        Table outerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
        outerTable.setWidth(UnitValue.createPercentValue(100));
        outerTable.setBorder(Border.NO_BORDER);
        outerTable.setMarginBottom(6);

        // --- Columna izquierda: Cliente ---
        Cell leftCell = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(6);

        leftCell.add(new Paragraph("INFORMACIÓN DEL CLIENTE")
                .setFontSize(9).setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setMarginBottom(4));

        Table clienteTable = new Table(UnitValue.createPercentArray(new float[]{38, 62}));
        clienteTable.setWidth(UnitValue.createPercentValue(100));

        agregarFilaTablaCompacta(clienteTable, "Nombre:", cliente.getNombreCompleto());
        agregarFilaTablaCompacta(clienteTable, "Documento:", persona.getTipoDocumento().getDescripcion() + " " + persona.getDocumento());

        String email = cliente.getPrimerEmail();
        if (email != null && !email.isEmpty()) {
            agregarFilaTablaCompacta(clienteTable, "Email:", email);
        }

        String telefono = cliente.getPrimerTelefono();
        if (telefono != null && !telefono.isEmpty()) {
            agregarFilaTablaCompacta(clienteTable, "Teléfono:", telefono);
        }

        leftCell.add(clienteTable);
        outerTable.addCell(leftCell);

        // --- Columna derecha: Equipo ---
        Cell rightCell = new Cell().setBorder(Border.NO_BORDER).setPaddingLeft(6);

        rightCell.add(new Paragraph("INFORMACIÓN DEL EQUIPO")
                .setFontSize(9).setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setMarginBottom(4));

        Table equipoTable = new Table(UnitValue.createPercentArray(new float[]{38, 62}));
        equipoTable.setWidth(UnitValue.createPercentValue(100));

        agregarFilaTablaCompacta(equipoTable, "Tipo:", equipo.getTipoEquipo().getDescripcion());
        agregarFilaTablaCompacta(equipoTable, "Marca:", equipo.getMarca().getDescripcion());

        if (equipo.getModelo() != null) {
            agregarFilaTablaCompacta(equipoTable, "Modelo:", equipo.getModelo().getDescripcion());
        }
        if (equipo.getNumeroSerie() != null && !equipo.getNumeroSerie().isEmpty()) {
            agregarFilaTablaCompacta(equipoTable, "N° de Serie:", equipo.getNumeroSerie());
        }
        if (equipo.getColor() != null && !equipo.getColor().isEmpty()) {
            agregarFilaTablaCompacta(equipoTable, "Color:", equipo.getColor());
        }

        rightCell.add(equipoTable);
        outerTable.addCell(rightCell);

        document.add(outerTable);
    }

    private void agregarDetallesServicio(Document document, Servicio servicio) {
        Paragraph seccion = new Paragraph("DETALLES DEL SERVICIO")
                .setFontSize(9)
                .setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setMarginBottom(4);
        document.add(seccion);

        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
        table.setWidth(UnitValue.createPercentValue(100));

        agregarFilaTablaCompacta(table, "Estado:", servicio.getEstado().getDescripcion());
        agregarFilaTablaCompacta(table, "Fecha de Recepción:", servicio.getFechaRecepcion().format(DATE_FORMATTER));

        if (servicio.getFechaDevolucionPrevista() != null) {
            agregarFilaTablaCompacta(table, "Devolución Prevista:", servicio.getFechaDevolucionPrevista().format(DATE_FORMATTER));
        }

        agregarFilaTablaCompacta(table, "Tipo de Ingreso:", servicio.getTipoIngreso().getDescripcion());
        agregarFilaTablaCompacta(table, "Recepcionado por:", servicio.getEmpleadoRecepcion().getPersona().getNombreCompleto());

        if (servicio.getEsGarantia()) {
            agregarFilaTablaCompacta(table, "Es Garantía:", "SÍ");
            if (servicio.getServicioGarantia() != null) {
                agregarFilaTablaCompacta(table, "Servicio Original:", servicio.getServicioGarantia().getNumeroServicio());
            }
        }

        if (servicio.getFallaReportada() != null && !servicio.getFallaReportada().isEmpty()) {
            agregarFilaTablaCompacta(table, "Falla Reportada:", servicio.getFallaReportada());
        }

        if (servicio.getAbonaVisita()) {
            agregarFilaTablaCompacta(table, "Abona Visita:", "SÍ");
            agregarFilaTablaCompacta(table, "Monto Visita:", "$" + servicio.getMontoVisita().toString());
            agregarFilaTablaCompacta(table, "Monto Pagado:", "$" + servicio.getMontoPagado().toString());
        }

        document.add(table);
        document.add(new Paragraph("").setMarginBottom(6));
    }

    private void agregarComponentes(Document document, Servicio servicio) {
        if (servicio.getDetalleServicios() == null || servicio.getDetalleServicios().isEmpty()) {
            return;
        }

        Paragraph seccion = new Paragraph("COMPONENTES DEL EQUIPO")
                .setFontSize(9)
                .setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setMarginBottom(4);
        document.add(seccion);

        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 15, 45}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setFontSize(8);

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

        for (DetalleServicio detalle : servicio.getDetalleServicios()) {
            table.addCell(new Cell().add(new Paragraph(detalle.getComponente())));
            table.addCell(new Cell().add(new Paragraph(detalle.getPresente() ? "Presente" : "No Presente"))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(detalle.getComentario() != null ? detalle.getComentario() : "-")));
        }

        document.add(table);
        document.add(new Paragraph("").setMarginBottom(6));
    }

    private void agregarFirma(Document document, Servicio servicio) {
        if (servicio.getFirmaIngreso() == null || servicio.getFirmaIngreso().isEmpty()) {
            return;
        }

        Paragraph seccion = new Paragraph("FIRMA DEL CLIENTE")
                .setFontSize(9)
                .setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setMarginBottom(4);
        document.add(seccion);

        try {
            byte[] imageBytes = Base64.getDecoder().decode(servicio.getFirmaIngreso());
            Image firma = new Image(ImageDataFactory.create(imageBytes));
            firma.setWidth(180);
            firma.setHeight(80);
            document.add(firma);
        } catch (Exception e) {
            log.error("Error al agregar firma al PDF", e);
            document.add(new Paragraph("Error al cargar firma"));
        }

        document.add(new Paragraph("").setMarginBottom(4));
    }

    private void agregarPiePagina(Document document) {
        document.add(new Paragraph("").setMarginBottom(4));
        SolidLine footerLine = new SolidLine(0.5f);
        footerLine.setColor(ColorConstants.GRAY);
        document.add(new LineSeparator(footerLine));

        Paragraph pie = new Paragraph("TÉRMINOS Y CONDICIONES")
                .setFontSize(8)
                .setBold()
                .setMarginTop(6)
                .setMarginBottom(2);
        document.add(pie);

        String[] terminos = {
                "Garantía: Los repuestos instalados tienen garantía de 90 días por defecto de fabricación. La mano de obra tiene garantía de 30 días. No se cubre daño por mal uso, caídas, humedad o causas externas.",
                "Almacenamiento: Los equipos no retirados dentro de los 90 días corridos desde la comunicación de finalización del servicio podrán generar un cargo adicional de almacenamiento.",
                "Conformidad: El retiro del equipo implica la conformidad con los trabajos efectuados y los precios acordados.",
                "Este documento constituye un comprobante de entrega del equipo reparado en las condiciones descritas."
        };

        for (String termino : terminos) {
            document.add(new Paragraph("• " + termino)
                    .setFontSize(7)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMarginBottom(2));
        }
    }

    private void agregarFilaTabla(Table table, String etiqueta, String valor) {
        Cell cellEtiqueta = new Cell().add(new Paragraph(etiqueta).setBold());
        Cell cellValor = new Cell().add(new Paragraph(valor != null ? valor : "-"));
        table.addCell(cellEtiqueta);
        table.addCell(cellValor);
    }

    private void agregarFilaTablaCompacta(Table table, String etiqueta, String valor) {
        Cell cellEtiqueta = new Cell()
                .add(new Paragraph(etiqueta).setBold().setFontSize(8))
                .setPadding(3);
        Cell cellValor = new Cell()
                .add(new Paragraph(valor != null ? valor : "-").setFontSize(8))
                .setPadding(3);
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
            document.setMargins(20, 36, 20, 36);

            agregarEncabezadoFinal(document, servicio);
            agregarInformacionClienteYEquipo(document, servicio);
            agregarDetallesServicio(document, servicio);
            agregarPresupuestoAprobado(document, servicio);
            agregarConformidad(document, servicio);
            agregarPiePagina(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF final para servicio {}", servicioId, e);
            throw new RuntimeException("Error al generar PDF final", e);
        }
    }

    private void agregarEncabezadoFinal(Document document, Servicio servicio) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{55, 45}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setBorder(Border.NO_BORDER);
        headerTable.setMarginBottom(6);

        Cell leftCell = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(0);

        try {
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("logos/logo-horizontal-original.png");
            if (logoStream != null) {
                Image logo = new Image(ImageDataFactory.create(logoStream.readAllBytes()));
                logo.setWidth(170);
                logo.setAutoScaleHeight(true);
                leftCell.add(logo);
            } else {
                leftCell.add(new Paragraph("Arroyo Electromecánica")
                        .setFontSize(12).setBold()
                        .setFontColor(new DeviceRgb(28, 96, 145)));
            }
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo", e);
            leftCell.add(new Paragraph("Arroyo Electromecánica")
                    .setFontSize(12).setBold()
                    .setFontColor(new DeviceRgb(28, 96, 145)));
        }

        Cell rightCell = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(0);

        rightCell.add(new Paragraph("COMPROBANTE FINAL DE SERVICIO TÉCNICO")
                .setFontSize(8)
                .setBold()
                .setFontColor(new DeviceRgb(80, 80, 80))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(2));

        rightCell.add(new Paragraph("Nº " + servicio.getNumeroServicio())
                .setFontSize(16)
                .setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(2));

        rightCell.add(new Paragraph("Estado: " + servicio.getEstado().getDescripcion())
                .setFontSize(8)
                .setFontColor(new DeviceRgb(80, 80, 80))
                .setTextAlignment(TextAlignment.RIGHT));

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        SolidLine line = new SolidLine(1f);
        line.setColor(new DeviceRgb(28, 96, 145));
        document.add(new LineSeparator(line));
        document.add(new Paragraph("").setMarginBottom(6));
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
                .setFontSize(9)
                .setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setMarginBottom(4);
        document.add(seccion);

        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
        table.setWidth(UnitValue.createPercentValue(100));

        agregarFilaTablaCompacta(table, "Nº Presupuesto:", presupuestoAprobado.getNumeroPresupuesto());

        if (presupuestoAprobado.getDiagnostico() != null && !presupuestoAprobado.getDiagnostico().isEmpty()) {
            agregarFilaTablaCompacta(table, "Diagnóstico:", presupuestoAprobado.getDiagnostico());
        }

        agregarFilaTablaCompacta(table, "Mano de Obra:", "$" + presupuestoAprobado.getManoObra().toString());
        agregarFilaTablaCompacta(table, "Repuestos:", "$" + presupuestoAprobado.getMontoRepuestosOriginal().toString());

        Cell labelTotal = new Cell().add(new Paragraph("Total:").setBold().setFontSize(8)).setPadding(3);
        Cell valorTotal = new Cell().add(new Paragraph("$" + presupuestoAprobado.getMontoTotalOriginal().toString())
                .setBold().setFontSize(8).setFontColor(new DeviceRgb(28, 96, 145))).setPadding(3);
        table.addCell(labelTotal);
        table.addCell(valorTotal);

        document.add(table);
        document.add(new Paragraph("").setMarginBottom(6));
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
                .setFontSize(9)
                .setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setMarginBottom(4);
        document.add(seccion);

        Paragraph textoConformidad = new Paragraph(
                "El cliente declara haber recibido el equipo en condiciones satisfactorias " +
                        "y de conformidad con el servicio realizado."
        )
                .setFontSize(8)
                .setMarginBottom(4);
        document.add(textoConformidad);

        if (servicio.getFechaDevolucionReal() != null) {
            Paragraph fechaDevolucion = new Paragraph("Fecha de devolución: " + servicio.getFechaDevolucionReal().format(DATE_FORMATTER))
                    .setFontSize(8)
                    .setMarginBottom(4);
            document.add(fechaDevolucion);
        }

        if (servicio.getFirmaConformidad() != null && !servicio.getFirmaConformidad().isEmpty()) {
            Paragraph firmaLabel = new Paragraph("Firma de Conformidad:")
                    .setFontSize(8)
                    .setBold()
                    .setMarginBottom(4);
            document.add(firmaLabel);

            try {
                byte[] imageBytes = Base64.getDecoder().decode(servicio.getFirmaConformidad());
                Image firma = new Image(ImageDataFactory.create(imageBytes));
                firma.setWidth(180);
                firma.setHeight(80);
                document.add(firma);
            } catch (Exception e) {
                log.error("Error al agregar firma de conformidad al PDF", e);
                document.add(new Paragraph("Error al cargar firma de conformidad"));
            }
        }

        document.add(new Paragraph("").setMarginBottom(6));
    }

    private void agregarPiePaginaPresupuesto(Document document) {
        document.add(new Paragraph("\n"));
        SolidLine footerLine = new SolidLine(0.5f);
        footerLine.setColor(ColorConstants.GRAY);
        document.add(new LineSeparator(footerLine));

        Paragraph titulo = new Paragraph("CONDICIONES DEL PRESUPUESTO")
                .setFontSize(8)
                .setBold()
                .setMarginTop(10);
        document.add(titulo);

        String[] condiciones = {
                "Validez: El presente presupuesto tiene validez hasta la fecha indicada. Vencido dicho plazo, los valores quedan sujetos a actualización sin previo aviso.",
                "Precios: Los importes incluyen únicamente los repuestos y la mano de obra detallados. Cualquier trabajo adicional que surja durante la reparación será comunicado y requerirá aprobación previa del cliente.",
                "Garantía: Los repuestos nuevos instalados tienen garantía de 90 días por defecto de fabricación. La mano de obra tiene una garantía de 30 días. No se cubre daño por mal uso o causas externas.",
                "Plazos: El plazo de reparación es estimado y puede verse afectado por la disponibilidad de repuestos u otras circunstancias técnicas.",
                "Retiro: El retiro del equipo una vez realizada la reparación implica la conformidad con los trabajos efectuados y los precios acordados.",
                "Almacenamiento: Los equipos no retirados dentro de los 90 días corridos desde la comunicación de finalización del servicio podrán generar un cargo adicional de almacenamiento."
        };

        for (String condicion : condiciones) {
            document.add(new Paragraph("• " + condicion)
                    .setFontSize(7)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMarginBottom(2));
        }
    }

    // ==================== PDF PRESUPUESTO ====================

    @Override
    public byte[] generarPdfPresupuesto(Long presupuestoId, Boolean mostrarOriginal, Boolean mostrarAlternativo) {
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            document.setMargins(20, 36, 20, 36);

            agregarEncabezadoPresupuesto(document, presupuesto);
            agregarInformacionClienteYEquipo(document, presupuesto.getServicio());
            agregarDetallePresupuesto(document, presupuesto, mostrarOriginal, mostrarAlternativo);
            agregarPiePaginaPresupuesto(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF de presupuesto {}", presupuestoId, e);
            throw new RuntimeException("Error al generar PDF de presupuesto", e);
        }
    }

    private void agregarEncabezadoPresupuesto(Document document, Presupuesto presupuesto) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{55, 45}));
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setBorder(Border.NO_BORDER);
        headerTable.setMarginBottom(6);

        Cell leftCell = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(0);

        try {
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("logos/logo-horizontal-original.png");
            if (logoStream != null) {
                Image logo = new Image(ImageDataFactory.create(logoStream.readAllBytes()));
                logo.setWidth(170);
                logo.setAutoScaleHeight(true);
                leftCell.add(logo);
            } else {
                leftCell.add(new Paragraph("Arroyo Electromecánica")
                        .setFontSize(12).setBold()
                        .setFontColor(new DeviceRgb(28, 96, 145)));
            }
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo", e);
            leftCell.add(new Paragraph("Arroyo Electromecánica")
                    .setFontSize(12).setBold()
                    .setFontColor(new DeviceRgb(28, 96, 145)));
        }

        Cell rightCell = new Cell().setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(0);

        rightCell.add(new Paragraph("PRESUPUESTO")
                .setFontSize(8).setBold()
                .setFontColor(new DeviceRgb(80, 80, 80))
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(2));

        rightCell.add(new Paragraph("Nº " + presupuesto.getNumeroPresupuesto())
                .setFontSize(16).setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setTextAlignment(TextAlignment.RIGHT));

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        SolidLine line = new SolidLine(1f);
        line.setColor(new DeviceRgb(28, 96, 145));
        document.add(new LineSeparator(line));
        document.add(new Paragraph("").setMarginBottom(6));
    }

    private void agregarDetallePresupuesto(Document document, Presupuesto presupuesto,
                                           Boolean mostrarOriginal, Boolean mostrarAlternativo) {
        Paragraph seccion = new Paragraph("DETALLE DEL PRESUPUESTO")
                .setFontSize(9).setBold()
                .setFontColor(new DeviceRgb(28, 96, 145))
                .setMarginBottom(4);
        document.add(seccion);

        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
        table.setWidth(UnitValue.createPercentValue(100));

        if (presupuesto.getFechaVencimiento() != null) {
            agregarFilaTablaCompacta(table, "Válido hasta:", presupuesto.getFechaVencimiento().format(DATE_FORMATTER));
        }

        if (presupuesto.getDiagnostico() != null && !presupuesto.getDiagnostico().isEmpty()) {
            agregarFilaTablaCompacta(table, "Diagnóstico:", presupuesto.getDiagnostico());
        }

        document.add(table);
        document.add(new Paragraph("").setMarginBottom(6));

        // Bloque de precio original
        if (mostrarOriginal != null && mostrarOriginal) {
            String titulo = (mostrarAlternativo != null && mostrarAlternativo)
                    ? "OPCIÓN 1 – REPUESTOS ORIGINALES"
                    : "DETALLE DE PRECIOS";

            Paragraph tituloOriginal = new Paragraph(titulo)
                    .setFontSize(9).setBold()
                    .setFontColor(new DeviceRgb(28, 96, 145))
                    .setMarginBottom(4);
            document.add(tituloOriginal);

            Table tOriginal = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
            tOriginal.setWidth(UnitValue.createPercentValue(100));
            agregarFilaTablaCompacta(tOriginal, "Repuestos:", "$" + presupuesto.getMontoRepuestosOriginal());
            agregarFilaTablaCompacta(tOriginal, "Mano de Obra:", "$" + presupuesto.getManoObra());

            Cell labelTotal = new Cell().add(new Paragraph("TOTAL:").setBold().setFontSize(9)).setPadding(3);
            Cell valorTotal = new Cell().add(new Paragraph("$" + presupuesto.getMontoTotalOriginal())
                    .setBold().setFontSize(9).setFontColor(new DeviceRgb(28, 96, 145))).setPadding(3);
            tOriginal.addCell(labelTotal);
            tOriginal.addCell(valorTotal);

            document.add(tOriginal);
            document.add(new Paragraph("").setMarginBottom(6));
        }

        // Bloque de precio alternativo
        if (mostrarAlternativo != null && mostrarAlternativo && presupuesto.getMontoRepuestosAlternativo() != null) {
            Paragraph tituloAlt = new Paragraph("OPCIÓN 2 – REPUESTOS ALTERNATIVOS")
                    .setFontSize(9).setBold()
                    .setFontColor(new DeviceRgb(28, 96, 145))
                    .setMarginBottom(4);
            document.add(tituloAlt);

            Table tAlt = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
            tAlt.setWidth(UnitValue.createPercentValue(100));
            agregarFilaTablaCompacta(tAlt, "Repuestos:", "$" + presupuesto.getMontoRepuestosAlternativo());
            agregarFilaTablaCompacta(tAlt, "Mano de Obra:", "$" + presupuesto.getManoObra());

            Cell labelTotalAlt = new Cell().add(new Paragraph("TOTAL:").setBold().setFontSize(9)).setPadding(3);
            Cell valorTotalAlt = new Cell().add(new Paragraph("$" + presupuesto.getMontoTotalAlternativo())
                    .setBold().setFontSize(9).setFontColor(new DeviceRgb(28, 96, 145))).setPadding(3);
            tAlt.addCell(labelTotalAlt);
            tAlt.addCell(valorTotalAlt);

            document.add(tAlt);
            document.add(new Paragraph("").setMarginBottom(6));
        }
    }
}
