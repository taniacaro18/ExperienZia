package com.experienzia.dto;

import com.experienzia.entity.EstadoEvento;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * Objeto de transferencia (DTO) para franja ocupacion salon. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class FranjaOcupacionSalonDTO {
    /** Id del evento al que pertenece */
    private Long eventoId;
    /** Dato del campo nombre evento */
    private String nombreEvento;
    /** Estado actual (ACTIVO, PENDIENTE, etc.) */
    private EstadoEvento estado;
    /** Dato del campo inicio */
    private LocalDateTime inicio;
    /** Dato del campo fin */
    private LocalDateTime fin;
    /** Campo `nombreOrganizador` (nombre organizador) */
    private String nombreOrganizador;
}
