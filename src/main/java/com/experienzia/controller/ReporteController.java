package com.experienzia.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experienzia.dto.AsistenciaDTO;
import com.experienzia.dto.DashboardAdminDTO;
import com.experienzia.dto.DashboardOrganizadorDTO;
import com.experienzia.dto.EventoPopularDTO;
import com.experienzia.dto.ReporteEventoAvanzadoDTO;
import com.experienzia.dto.ReporteEventoDTO;
import com.experienzia.dto.ReportePagosAdminDTO;
import com.experienzia.dto.ReporteUsuariosAdminDTO;
import com.experienzia.dto.ResumenDTO;
import com.experienzia.entity.Rol;
import com.experienzia.security.SecurityAccessHelper;
import com.experienzia.service.ReporteService;

// Gráficas y tablas del front: dashboards, asistencia, eventos populares
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/eventos-populares")
    public ResponseEntity<List<EventoPopularDTO>> eventosPopulares() {
        return ResponseEntity.ok(reporteService.obtenerEventosPopulares());
    }

    // Cuántos inscritos vs cuántos hicieron check-in en un evento
    @GetMapping("/asistencia/{eventoId}")
    public ResponseEntity<AsistenciaDTO> asistenciaPorEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(reporteService.obtenerAsistenciaPorEvento(eventoId));
    }

    @GetMapping("/usuarios/{eventoId}")
    public ResponseEntity<List<Long>> usuariosPorEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(reporteService.obtenerUsuariosPorEvento(eventoId));
    }

    // Números globales para el dashboard del admin
    @GetMapping("/resumen")
    public ResponseEntity<ResumenDTO> resumenGeneral() {
        return ResponseEntity.ok(reporteService.obtenerResumenGeneral());
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<ReporteEventoDTO> reporteDetallado(
            @PathVariable Long eventoId,
            @RequestParam(required = false) Long organizadorId) {
        return ResponseEntity.ok(reporteService.obtenerReporteDetalladoEvento(eventoId, organizadorId));
    }

    // Reporte con más gráficas; organizadorId opcional para que no vea eventos ajenos
    @GetMapping("/evento/{eventoId}/avanzado")
    public ResponseEntity<ReporteEventoAvanzadoDTO> reporteAvanzado(
            @PathVariable Long eventoId,
            @RequestParam(required = false) Long organizadorId) {
        return ResponseEntity.ok(reporteService.obtenerReporteAvanzadoEvento(eventoId, organizadorId));
    }

    @GetMapping("/dashboard/organizador/{organizadorId}")
    public ResponseEntity<DashboardOrganizadorDTO> dashboardOrganizador(@PathVariable Long organizadorId) {
        return ResponseEntity.ok(reporteService.obtenerDashboardOrganizador(organizadorId));
    }

    @GetMapping("/dashboard/admin")
    public ResponseEntity<DashboardAdminDTO> dashboardAdmin() {
        return ResponseEntity.ok(reporteService.obtenerDashboardAdmin());
    }

    @GetMapping("/admin/pagos")
    public ResponseEntity<ReportePagosAdminDTO> reportePagosAdmin() {
        SecurityAccessHelper.requireRol(Rol.ADMIN);
        return ResponseEntity.ok(reporteService.obtenerReportePagosAdmin());
    }

    @GetMapping("/admin/usuarios")
    public ResponseEntity<ReporteUsuariosAdminDTO> reporteUsuariosAdmin() {
        SecurityAccessHelper.requireRol(Rol.ADMIN);
        return ResponseEntity.ok(reporteService.obtenerReporteUsuariosAdmin());
    }
}
