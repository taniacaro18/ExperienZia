package com.experienzia.dto;

import java.util.List;

import lombok.Data;


@Data

public class DashboardOrganizadorDTO {

    private Long organizadorId;

    private long eventosActivos;

    private long eventosPendientes;

    private long eventosCancelados;

    private long eventosTotales;

    private long totalInscritos;

    private int aforoMaximoPorEvento;

    private long cuposOcupadosEventosActivos;

    private long asistenciasUltimos30Dias;

    private List<PuntoSerieDTO> serieMensualEventos;
    
    private List<PuntoSerieDTO> serieMensualInscripciones;
}
