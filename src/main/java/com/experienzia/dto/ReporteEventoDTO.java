package com.experienzia.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Reporte básico que armo para el organizador: KPIs del evento + tabla de asistentes.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReporteEventoDTO {

    // Cabecera del evento
    private Long eventoId;

    private String nombreEvento;

    private String estadoEvento;

    private LocalDateTime fechaEvento;

    private Integer duracionHoras;

    // Métricas de aforo y asistencia
    private int aforoMaximo;

    private long inscritos;

    private long asistenciasReales;

    private long asistentesActualmenteEnSala;

    private double porcentajeOcupacion;

    private double porcentajeAsistenciaSobreInscritos;

    // Detalle fila por fila (export CSV/PDF o pantalla)
    private List<AsistenteEventoDTO> asistentes;
}
