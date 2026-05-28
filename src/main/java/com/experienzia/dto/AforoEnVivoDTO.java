package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Devuelvo esto en el endpoint de aforo en vivo — lo pinta el staff/organizador durante el evento.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AforoEnVivoDTO {

    private Long eventoId;

    private String nombreEvento;

    // Cupos y contadores
    private int aforoMaximo;

    private long inscritos;

    private long asistencias;

    private long presentes;

    private long cuposDisponibles;

    private double porcentajeOcupacion;
}
