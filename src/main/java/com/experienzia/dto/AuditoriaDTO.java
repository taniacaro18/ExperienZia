package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Registro de una acción en el sistema (auditoría).
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class AuditoriaDTO {
    /** Identificador único en la base de datos */
    private Long id;
    /** Id del usuario relacionado */
    private Long usuarioId;
    /** Campo `accion` (accion) */
    private String accion;
    /** Dato del campo entidad */
    private String entidad;
    /** Dato del campo entidad id */
    private Long entidadId;
    /** Fecha relacionada con la operación */
    private LocalDateTime fecha;
    /** Dato del campo direccion ip */
    private String direccionIp;
}
