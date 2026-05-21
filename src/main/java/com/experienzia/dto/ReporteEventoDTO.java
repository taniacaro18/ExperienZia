package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para reporte evento. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class ReporteEventoDTO {
    /** Id del evento al que pertenece */
    private Long eventoId;
    /** Dato del campo nombre evento */
    private String nombreEvento;
    /** Campo `estadoEvento` (estado evento) */
    private String estadoEvento;
    /** Dato del campo fecha evento */
    private LocalDateTime fechaEvento;
    /** Dato del campo duracion horas */
    private Integer duracionHoras;
    /** Campo `aforoMaximo` (aforo maximo) */
    private int aforoMaximo;
    /** Dato del campo inscritos */
    private long inscritos;
    /** Dato del campo asistencias reales */
    private long asistenciasReales;
    /** Campo `asistentesActualmenteEnSala` (asistentes actualmente en sala) */
    private long asistentesActualmenteEnSala;
    /** Dato del campo porcentaje ocupacion */
    private double porcentajeOcupacion;
    /** Dato del campo porcentaje asistencia sobre inscritos */
    private double porcentajeAsistenciaSobreInscritos;
    /** Campo `asistentes` (asistentes) */
    private List<AsistenteEventoDTO> asistentes;
}
