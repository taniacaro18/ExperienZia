package com.experienzia.dto;

import lombok.Data;

import java.util.List;

/**
 * Métricas para el dashboard del organizador (vista "Mis eventos / Mis números").
 */
@Data
/**
 * Objeto de transferencia (DTO) para dashboard organizador. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class DashboardOrganizadorDTO {
    /** Dato del campo organizador id */
    private Long organizadorId;
    /** Dato del campo eventos activos */
    private long eventosActivos;
    /** Campo `eventosPendientes` (eventos pendientes) */
    private long eventosPendientes;
    /** Dato del campo eventos cancelados */
    private long eventosCancelados;
    /** Dato del campo eventos totales */
    private long eventosTotales;
    /** Campo `totalInscritos` (total inscritos) */
    private long totalInscritos;
    /** Límite de cupos por evento (regla de negocio; no es capacidad global del salón). */
    private int aforoMaximoPorEvento;
    /** Suma de inscritos/presentes solo en eventos ACTIVO del organizador. */
    private long cuposOcupadosEventosActivos;
    /** Dato del campo asistencias ultimos30 dias */
    private long asistenciasUltimos30Dias;
    /** Dato del campo serie mensual eventos */
    private List<PuntoSerieDTO> serieMensualEventos;
    /** Campo `serieMensualInscripciones` (serie mensual inscripciones) */
    private List<PuntoSerieDTO> serieMensualInscripciones;
}
