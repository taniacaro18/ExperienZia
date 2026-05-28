package com.experienzia.service;

import com.experienzia.dto.AsistenciaDTO;
import com.experienzia.dto.DashboardAdminDTO;
import com.experienzia.dto.DashboardOrganizadorDTO;
import com.experienzia.dto.EventoPopularDTO;
import com.experienzia.dto.ReporteEventoAvanzadoDTO;
import com.experienzia.dto.ReporteEventoDTO;
import com.experienzia.dto.ReportePagosAdminDTO;
import com.experienzia.dto.ReporteUsuariosAdminDTO;
import com.experienzia.dto.ResumenDTO;

import java.util.List;

// Reportes y dashboards: saco números de la BD para pintar gráficas y tablas en el front
public interface ReporteService {

    // Eventos ordenados por inscritos (más populares primero)
    List<EventoPopularDTO> obtenerEventosPopulares();

    // Cuántos asistieron de verdad a un evento (check-in)
    AsistenciaDTO obtenerAsistenciaPorEvento(Long eventoId);

    // Ids de inscritos (notificaciones masivas u otros flujos)
    List<Long> obtenerUsuariosPorEvento(Long eventoId);

    // Totales globales del sistema para el admin
    ResumenDTO obtenerResumenGeneral();

    // Reporte detallado de un evento para su organizador (aforo, lista, % ocupación)
    ReporteEventoDTO obtenerReporteDetalladoEvento(Long eventoId, Long organizadorId);

    // Reporte avanzado: ingresos por hora, QR vs manual, desempeño staff
    ReporteEventoAvanzadoDTO obtenerReporteAvanzadoEvento(Long eventoId, Long organizadorId);

    // Tarjetas del panel principal del organizador
    DashboardOrganizadorDTO obtenerDashboardOrganizador(Long organizadorId);

    // Tarjetas del panel del administrador
    DashboardAdminDTO obtenerDashboardAdmin();

    // Reporte analítico de pagos (admin)
    ReportePagosAdminDTO obtenerReportePagosAdmin();

    // Reporte de usuarios y crecimiento (admin)
    ReporteUsuariosAdminDTO obtenerReporteUsuariosAdmin();
}
