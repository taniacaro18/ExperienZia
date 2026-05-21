package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos del staff asignado a un evento, incluyendo su función específica.
 * Se usa en GET /api/eventos/{id}/staff (para que el organizador vea quién hace qué).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para staff asignado. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class StaffAsignadoDTO {
    /** Dato del campo asignacion id */
    private Long asignacionId;
    /** Dato del campo staff usuario id */
    private Long staffUsuarioId;
    /** Nombre completo de la persona */
    private String nombre;
    /** Correo electrónico del usuario */
    private String email;
    /** Número de teléfono de contacto */
    private String telefono;
    /** Campo `estadoUsuario` (estado usuario) */
    private String estadoUsuario;
    /** Dato del campo funcion */
    private String funcion;
}
