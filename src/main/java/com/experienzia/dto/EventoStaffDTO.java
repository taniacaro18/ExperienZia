package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Datos del evento al que un staff está asignado, incluyendo su función específica.
 * Se usa en GET /api/staff/{staffUsuarioId}/eventos para alimentar el panel del staff.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para evento staff. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class EventoStaffDTO {
    /** Dato del campo asignacion id */
    private Long asignacionId;
    /** Id del evento al que pertenece */
    private Long eventoId;
    /** Campo `nombreEvento` (nombre evento) */
    private String nombreEvento;
    /** Dato del campo descripcion */
    private String descripcion;
    /** Dato del campo fecha evento */
    private LocalDateTime fechaEvento;
    /** Campo `ubicacion` (ubicacion) */
    private String ubicacion;
    /** Dato del campo tipo evento */
    private String tipoEvento;
    /** Dato del campo estado evento */
    private String estadoEvento;
    /** Campo `categoria` (categoria) */
    private String categoria;
    /** Dato del campo aforo maximo */
    private Integer aforoMaximo;
    /** Dato del campo aforo actual */
    private Integer aforoActual;
    /** Campo `organizadorId` (organizador id) */
    private Long organizadorId;
    /** Dato del campo funcion */
    private String funcion;
}
