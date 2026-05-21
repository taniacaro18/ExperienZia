package com.experienzia.controller;

import com.experienzia.dto.AsistenciaDTO;
import com.experienzia.dto.DashboardAdminDTO;
import com.experienzia.dto.DashboardOrganizadorDTO;
import com.experienzia.dto.EventoPopularDTO;
import com.experienzia.dto.ReporteEventoAvanzadoDTO;
import com.experienzia.dto.ReporteEventoDTO;
import com.experienzia.dto.ResumenDTO;
import com.experienzia.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Aquí están los endpoints de estadísticas y reportes (no modifican datos, solo consultan)
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // GET /api/reportes/eventos-populares — ranking de eventos con más inscripciones
    @GetMapping("/eventos-populares")
    public ResponseEntity<List<EventoPopularDTO>> eventosPopulares() {
        return ResponseEntity.ok(reporteService.obtenerEventosPopulares());
    }

    // GET /api/reportes/asistencia/{eventoId} — datos de asistencia de un evento
    @GetMapping("/asistencia/{eventoId}")
    public ResponseEntity<AsistenciaDTO> asistenciaPorEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(reporteService.obtenerAsistenciaPorEvento(eventoId));
    }

    // GET /api/reportes/usuarios/{eventoId} — ids de usuarios inscritos en el evento
    @GetMapping("/usuarios/{eventoId}")
    public ResponseEntity<List<Long>> usuariosPorEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(reporteService.obtenerUsuariosPorEvento(eventoId));
    }

    // GET /api/reportes/resumen — números globales del sistema (totales, etc.)
    @GetMapping("/resumen")
    public ResponseEntity<ResumenDTO> resumenGeneral() {
        return ResponseEntity.ok(reporteService.obtenerResumenGeneral());
    }

    /** HU-023: reporte detallado por evento. */
    // GET /api/reportes/evento/{eventoId}?organizadorId=... — reporte para organizador o admin
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<ReporteEventoDTO> reporteDetallado(
            @PathVariable Long eventoId,
            @RequestParam(required = false) Long organizadorId) {
        return ResponseEntity.ok(reporteService.obtenerReporteDetalladoEvento(eventoId, organizadorId));
    }

    /** Reporte avanzado: curva por hora, QR vs manual, desempeño staff. */
    @GetMapping("/evento/{eventoId}/avanzado")
    public ResponseEntity<ReporteEventoAvanzadoDTO> reporteAvanzado(
            @PathVariable Long eventoId,
            @RequestParam(required = false) Long organizadorId) {
        return ResponseEntity.ok(reporteService.obtenerReporteAvanzadoEvento(eventoId, organizadorId));
    }

    /** Dashboard del organizador (KPIs + serie mensual). */
    @GetMapping("/dashboard/organizador/{organizadorId}")
    public ResponseEntity<DashboardOrganizadorDTO> dashboardOrganizador(@PathVariable Long organizadorId) {
        return ResponseEntity.ok(reporteService.obtenerDashboardOrganizador(organizadorId));
    }

    /** Dashboard del administrador (KPIs globales + serie mensual). */
    @GetMapping("/dashboard/admin")
    public ResponseEntity<DashboardAdminDTO> dashboardAdmin() {
        return ResponseEntity.ok(reporteService.obtenerDashboardAdmin());
    }
}
