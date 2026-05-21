package com.experienzia.service;

import com.experienzia.dto.AsistenciaDTO;
import com.experienzia.dto.DashboardAdminDTO;
import com.experienzia.dto.DashboardOrganizadorDTO;
import com.experienzia.dto.EventoPopularDTO;
import com.experienzia.dto.ReporteEventoAvanzadoDTO;
import com.experienzia.dto.ReporteEventoDTO;
import com.experienzia.dto.ResumenDTO;

import java.util.List;

/**
 * Interfaz del servicio de reportes y estadísticas.
 * Agrupa consultas para dashboards, popularidad de eventos y métricas de asistencia.
 */
/**
 * Interfaz del servicio ReporteService.
 * Define qué operaciones puede hacer el backend; la clase *Impl las programa.
 */
public interface ReporteService {

    /** Eventos ordenados por cantidad de inscritos (más populares primero). */
    List<EventoPopularDTO> obtenerEventosPopulares();

    /** Cuenta cuántas personas asistieron realmente a un evento. */
    AsistenciaDTO obtenerAsistenciaPorEvento(Long eventoId);

    /** Devuelve los ids de usuarios inscritos en un evento. */
    List<Long> obtenerUsuariosPorEvento(Long eventoId);

    /** Totales globales: usuarios, eventos e inscripciones en el sistema. */
    ResumenDTO obtenerResumenGeneral();

    /**
     * Reporte detallado de un evento para el organizador (aforo, % ocupación, lista de asistentes).
     * HU-023.
     */
    ReporteEventoDTO obtenerReporteDetalladoEvento(Long eventoId, Long organizadorId);

    /**
     * Reporte avanzado: curva de ingresos por hora, check-in QR vs manual, desempeño del staff.
     */
    ReporteEventoAvanzadoDTO obtenerReporteAvanzadoEvento(Long eventoId, Long organizadorId);

    /** Números resumidos para el panel principal del organizador. */
    DashboardOrganizadorDTO obtenerDashboardOrganizador(Long organizadorId);

    /** Números resumidos para el panel del administrador. */
    DashboardAdminDTO obtenerDashboardAdmin();
}
