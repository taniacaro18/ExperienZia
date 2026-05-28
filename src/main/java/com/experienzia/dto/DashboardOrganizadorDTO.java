package com.experienzia.dto;

import java.util.List;

import lombok.Data;

// Respuesta del dashboard del organizador — solo números de SUS eventos, no de toda la plataforma.
@Data

public class DashboardOrganizadorDTO {

    private Long organizadorId;

    // Contadores por estado de evento
    private long eventosActivos;

    private long eventosPendientes;

    private long eventosCancelados;

    private long eventosTotales;

    // Inscripciones y aforo
    private long totalInscritos;

    private int aforoMaximoPorEvento;

    private long cuposOcupadosEventosActivos;

    private long asistenciasUltimos30Dias;

    // Series para gráficas en Angular
    private List<PuntoSerieDTO> serieMensualEventos;

    private List<PuntoSerieDTO> serieMensualInscripciones;
}
