package com.experienzia.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo devuelvo al front en "mis eventos" del staff — mezclo datos del evento y de la asignación.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class EventoStaffDTO {

    private Long asignacionId;

    private Long eventoId;

    // Info del evento para la tarjeta/lista
    private String nombreEvento;

    private String descripcion;

    private LocalDateTime fechaEvento;

    private String ubicacion;

    private String tipoEvento;

    private String estadoEvento;

    private String categoria;

    private Integer aforoMaximo;

    private Integer aforoActual;

    private Long organizadorId;

    // Qué le toca hacer en puerta (QR, manual, salida…)
    private String funcion;
}
