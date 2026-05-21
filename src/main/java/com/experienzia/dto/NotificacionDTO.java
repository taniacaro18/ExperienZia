package com.experienzia.dto;

import com.experienzia.entity.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Mensaje de notificación para el usuario.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class NotificacionDTO {
    /** Identificador único en la base de datos */
    private Long id;
    /** Id del usuario relacionado */
    private Long usuarioId;
    /** Texto que se muestra al usuario */
    private String mensaje;
    /** Tipo o categoría según el contexto */
    private TipoNotificacion tipo;
    /** Dato del campo leida */
    private boolean leida;
    /** Fecha relacionada con la operación */
    private LocalDateTime fecha;
}
