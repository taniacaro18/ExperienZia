package com.experienzia.dto;

import com.experienzia.entity.EstadoInscripcion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Datos de la inscripción de un asistente a un evento.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class InscripcionDTO {
    /** Identificador único en la base de datos */
    private Long id;
    /** Id del usuario relacionado */
    private Long usuarioId;
    /** Id del evento al que pertenece */
    private Long eventoId;
    /** Dato del campo fecha inscripcion */
    private LocalDateTime fechaInscripcion;
    /** Estado actual (ACTIVO, PENDIENTE, etc.) */
    private EstadoInscripcion estado;
    /** Campo `fechaCheckIn` (fecha check in) */
    private LocalDateTime fechaCheckIn;
    /** Dato del campo fecha check out */
    private LocalDateTime fechaCheckOut;
    /** Dato del campo codigo q r */
    private String codigoQR;

    /** Completado solo en respuestas de check-in / check-out (QR o manual). */
    private String nombreAsistente;
    /** Dato del campo email asistente */
    private String emailAsistente;
    /** Dato del campo tipo documento */
    private String tipoDocumento;
    /** Campo `numeroDocumento` (numero documento) */
    private String numeroDocumento;
    /** Dato del campo nombre evento */
    private String nombreEvento;
    /** Dato del campo fecha evento */
    private LocalDateTime fechaEvento;
    /** Campo `fechaFinEvento` (fecha fin evento) */
    private LocalDateTime fechaFinEvento;
    /** Dato del campo ubicacion evento */
    private String ubicacionEvento;
}
