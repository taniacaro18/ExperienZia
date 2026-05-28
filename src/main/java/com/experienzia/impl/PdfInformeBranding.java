package com.experienzia.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Cabecera con logo y bloques KPI para que todos los PDF se vean ejecutivos y parejos
public final class PdfInformeBranding {

    private static final Color BRAND = new Color(124, 99, 196);
    private static final Color BRAND_LIGHT = new Color(248, 244, 255);
    private static final Color BORDER = new Color(220, 215, 235);
    private static final DateTimeFormatter FECHA_GEN =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private PdfInformeBranding() {
    }

    public static float escribirCabecera(Document doc, PdfWriter writer, String titulo, String subtitulo) {
        PdfPTable bar = new PdfPTable(new float[]{1.2f, 4f});
        bar.setWidthPercentage(100);
        bar.setSpacingAfter(12f);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Image logo = cargarLogo();
        if (logo != null) {
            logo.scaleToFit(72f, 48f);
            logoCell.addElement(logo);
        } else {
            Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BRAND);
            logoCell.addElement(new Paragraph("ExperienZia", f));
        }

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
        Paragraph pTitle = new Paragraph(titulo, titleFont);
        pTitle.setSpacingAfter(4f);
        textCell.addElement(pTitle);
        if (subtitulo != null && !subtitulo.isBlank()) {
            textCell.addElement(new Paragraph(subtitulo, subFont));
        }
        textCell.addElement(new Paragraph(
                "Generado: " + LocalDateTime.now().format(FECHA_GEN), subFont));

        bar.addCell(logoCell);
        bar.addCell(textCell);
        try {
            doc.add(bar);
        } catch (Exception ex) {
            throw new IllegalStateException("No pude dibujar la cabecera del PDF.", ex);
        }

        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase(" "));
        c.setFixedHeight(3f);
        c.setBackgroundColor(BRAND);
        c.setBorder(Rectangle.NO_BORDER);
        line.addCell(c);
        try {
            doc.add(line);
            doc.add(new Paragraph(" "));
        } catch (Exception ex) {
            throw new IllegalStateException("No pude dibujar la línea de marca en PDF.", ex);
        }
        return doc.topMargin();
    }

    public static void agregarTarjetasKpi(Document doc, String[][] kpis) {
        if (kpis == null || kpis.length == 0) {
            return;
        }
        int cols = Math.min(4, kpis.length);
        PdfPTable grid = new PdfPTable(cols);
        grid.setWidthPercentage(100);
        grid.setSpacingAfter(14f);
        Font labelF = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Font valueF = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND);

        for (String[] kpi : kpis) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(BRAND_LIGHT);
            cell.setBorderColor(BORDER);
            cell.setBorderWidth(0.6f);
            cell.setPadding(10f);
            cell.addElement(new Paragraph(kpi[0], labelF));
            cell.addElement(new Paragraph(kpi[1], valueF));
            grid.addCell(cell);
        }
        int resto = kpis.length % cols;
        if (resto != 0) {
            for (int i = 0; i < cols - resto; i++) {
                PdfPCell empty = new PdfPCell(new Phrase(" "));
                empty.setBorder(Rectangle.NO_BORDER);
                grid.addCell(empty);
            }
        }
        try {
            doc.add(grid);
        } catch (Exception ex) {
            throw new IllegalStateException("No pude dibujar las tarjetas KPI del PDF.", ex);
        }
    }

    public static PdfPCell celdaEncabezadoTabla(String texto) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setBackgroundColor(BRAND);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(6f);
        c.setBorderColor(BRAND);
        return c;
    }

    public static PdfPCell celdaCuerpoTabla(String texto, boolean alterno) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, f));
        c.setBackgroundColor(alterno ? BRAND_LIGHT : Color.WHITE);
        c.setPadding(5f);
        c.setBorderColor(BORDER);
        c.setBorderWidth(0.5f);
        return c;
    }

    private static Image cargarLogo() {
        try (InputStream in = PdfInformeBranding.class.getResourceAsStream("/static/logo.png")) {
            if (in == null) {
                return null;
            }
            byte[] bytes = in.readAllBytes();
            return Image.getInstance(bytes);
        } catch (Exception ex) {
            return null;
        }
    }
}
