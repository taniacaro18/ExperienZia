package com.experienzia.controller;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experienzia.dto.AsistenteEventoDTO;
import com.experienzia.dto.EventoDTO;
import com.experienzia.entity.Rol;
import com.experienzia.security.SecurityAccessHelper;
import com.experienzia.service.EventoService;
import com.experienzia.service.InscripcionService;
import com.experienzia.service.ReporteService;
import com.experienzia.service.export.ExportService;

// Descargas Excel/PDF para admin y organizador (no pasan por el front armando el archivo)
@RestController
@RequestMapping("/api/export")
public class ExportController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExportService exportService;
    private final EventoService eventoService;
    private final InscripcionService inscripcionService;
    private final ReporteService reporteService;

    public ExportController(ExportService exportService,
                            EventoService eventoService,
                            InscripcionService inscripcionService,
                            ReporteService reporteService) {
        this.exportService = exportService;
        this.eventoService = eventoService;
        this.inscripcionService = inscripcionService;
        this.reporteService = reporteService;
    }

    // Todos los eventos en Excel (admin)
    @GetMapping(value = "/eventos.xlsx")
    public ResponseEntity<ByteArrayResource> eventosExcel() {
        List<EventoDTO> lista = eventoService.listarTodos();
        byte[] data = exportService.eventosExcel(lista);
        return descarga(data, "eventos.xlsx", XLSX);
    }

    @GetMapping(value = "/eventos.pdf")
    public ResponseEntity<ByteArrayResource> eventosPdf() {
        List<EventoDTO> lista = eventoService.listarTodos();
        byte[] data = exportService.eventosPdf(lista);
        return descarga(data, "eventos.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(value = "/eventos/{eventoId}/asistentes.xlsx")
    public ResponseEntity<ByteArrayResource> asistentesExcel(
            @PathVariable Long eventoId,
            @RequestParam(required = false) Long organizadorId) {
        EventoDTO evento = eventoService.obtenerPorId(eventoId);
        List<AsistenteEventoDTO> asistentes = obtenerAsistentes(eventoId, organizadorId);
        byte[] data = exportService.resumenAsistentesExcel(evento, asistentes);
        String nombre = "asistentes_" + sanear(evento.getNombre()) + ".xlsx";
        return descarga(data, nombre, XLSX);
    }

    @GetMapping(value = {"/admin/reportes/pagos", "/reportes/admin/pagos.pdf"},
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<ByteArrayResource> reportePagosAdminPdf() {
        SecurityAccessHelper.requireRol(Rol.ADMIN);
        byte[] data = exportService.reportePagosAdminPdf(reporteService.obtenerReportePagosAdmin());
        return descarga(data, "reporte_pagos_experienzia.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(value = {"/admin/reportes/usuarios", "/reportes/admin/usuarios.pdf"},
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<ByteArrayResource> reporteUsuariosAdminPdf() {
        SecurityAccessHelper.requireRol(Rol.ADMIN);
        byte[] data = exportService.reporteUsuariosAdminPdf(reporteService.obtenerReporteUsuariosAdmin());
        return descarga(data, "reporte_usuarios_experienzia.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(value = "/eventos/{eventoId}/asistentes.pdf")
    public ResponseEntity<ByteArrayResource> asistentesPdf(
            @PathVariable Long eventoId,
            @RequestParam(required = false) Long organizadorId) {
        EventoDTO evento = eventoService.obtenerPorId(eventoId);
        List<AsistenteEventoDTO> asistentes = obtenerAsistentes(eventoId, organizadorId);
        byte[] data = exportService.resumenAsistentesPdf(evento, asistentes);
        String nombre = "asistentes_" + sanear(evento.getNombre()) + ".pdf";
        return descarga(data, nombre, MediaType.APPLICATION_PDF);
    }

    // Sin organizadorId devuelvo lista vacía — honestamente el front siempre debería mandarlo
    private List<AsistenteEventoDTO> obtenerAsistentes(Long eventoId, Long organizadorId) {
        if (organizadorId != null) {
            return inscripcionService.listarAsistentesParaOrganizador(eventoId, organizadorId, null);
        }
        return List.of();
    }

    private ResponseEntity<ByteArrayResource> descarga(byte[] data, String filename, MediaType tipo) {
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(data.length)
                .body(resource);
    }

    private String sanear(String nombre) {
        if (nombre == null || nombre.isBlank()) return "evento";
        return nombre.replaceAll("[^A-Za-z0-9_-]+", "_");
    }
}
