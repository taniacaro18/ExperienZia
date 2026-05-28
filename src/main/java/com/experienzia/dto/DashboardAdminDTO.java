package com.experienzia.dto;

import java.util.List;

import lombok.Data;

// Payload del dashboard admin — agrego contadores de toda la plataforma y series mensuales.
@Data

public class DashboardAdminDTO {

    // Eventos por estado
    private long eventosActivos;

    private long eventosPendientes;

    private long eventosCancelados;

    private long eventosTotales;

    // Usuarios y roles
    private long usuariosTotales;

    private long usuariosActivos;

    private long usuariosPendientes;

    private long organizadoresActivos;

    private long asistentesTotales;

    private long staffTotales;

    private long inscripcionesTotales;

    // Puntos mensuales para Chart.js o lo que use el front
    private List<PuntoSerieDTO> serieMensualEventos;
    
    private List<PuntoSerieDTO> serieMensualUsuarios;
}
