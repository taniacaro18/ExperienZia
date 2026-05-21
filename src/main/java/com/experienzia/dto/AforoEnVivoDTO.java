package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para aforo en vivo. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class AforoEnVivoDTO {
    /** Id del evento al que pertenece */
    private Long eventoId;
    /** Dato del campo nombre evento */
    private String nombreEvento;
    /** Campo `aforoMaximo` (aforo maximo) */
    private int aforoMaximo;
    /** Dato del campo inscritos */
    private long inscritos;
    /** Dato del campo asistencias */
    private long asistencias;
    /** Campo `presentes` (presentes) */
    private long presentes;
    /** Dato del campo cupos disponibles */
    private long cuposDisponibles;
    /** Dato del campo porcentaje ocupacion */
    private double porcentajeOcupacion;
}
