package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Datos del certificado de asistencia.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class CertificadoDTO {
    /** Identificador único en la base de datos */
    private Long id;
    /** Id del usuario relacionado */
    private Long usuarioId;
    /** Id del evento al que pertenece */
    private Long eventoId;
    /** Dato del campo fecha generacion */
    private LocalDateTime fechaGeneracion;
    /** Dato del campo codigo unico */
    private String codigoUnico;

    /** Campo `nombreAsistente` (nombre asistente) */
    private String nombreAsistente;
    /** Dato del campo numero documento */
    private String numeroDocumento;
    /** Dato del campo nombre evento */
    private String nombreEvento;
    /** Campo `fechaEvento` (fecha evento) */
    private LocalDateTime fechaEvento;
    /** Dato del campo duracion horas */
    private Integer duracionHoras;

    /** Nombre del organizador del evento (firma en el certificado). */
    private String nombreOrganizador;
    /** Ciudad de expedición (p. ej. desde la ubicación del evento). */
    private String ciudadExpedicion;
}
