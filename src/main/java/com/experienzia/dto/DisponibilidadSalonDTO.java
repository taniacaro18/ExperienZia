package com.experienzia.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
/**
 * Objeto de transferencia (DTO) para disponibilidad salon. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class DisponibilidadSalonDTO {
    /** Dato del campo ubicacion */
    private String ubicacion;
    /** Dato del campo desde */
    private LocalDateTime desde;
    /** Campo `hasta` (hasta) */
    private LocalDateTime hasta;
    /** Si se envió propuesta de horario, indica si no choca con ninguna ocupación. */
    private Boolean propuestaDisponible;
    /** Dato del campo mensaje propuesta */
    private String mensajePropuesta;
    private List<FranjaOcupacionSalonDTO> ocupaciones = new ArrayList<>();
}
