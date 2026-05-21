package com.experienzia.dto;

import lombok.Data;

import java.util.List;

/**
 * Métricas globales para el dashboard del administrador.
 */
@Data
/**
 * Objeto de transferencia (DTO) para dashboard admin. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class DashboardAdminDTO {
    /** Dato del campo eventos activos */
    private long eventosActivos;
    /** Dato del campo eventos pendientes */
    private long eventosPendientes;
    /** Campo `eventosCancelados` (eventos cancelados) */
    private long eventosCancelados;
    /** Dato del campo eventos totales */
    private long eventosTotales;
    /** Dato del campo usuarios totales */
    private long usuariosTotales;
    /** Campo `usuariosActivos` (usuarios activos) */
    private long usuariosActivos;
    /** Dato del campo usuarios pendientes */
    private long usuariosPendientes;
    /** Dato del campo organizadores activos */
    private long organizadoresActivos;
    /** Campo `asistentesTotales` (asistentes totales) */
    private long asistentesTotales;
    /** Dato del campo staff totales */
    private long staffTotales;
    /** Dato del campo inscripciones totales */
    private long inscripcionesTotales;
    /** Campo `serieMensualEventos` (serie mensual eventos) */
    private List<PuntoSerieDTO> serieMensualEventos;
    /** Dato del campo serie mensual usuarios */
    private List<PuntoSerieDTO> serieMensualUsuarios;
}
