package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para actualizar perfil. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class ActualizarPerfilDTO {
    /** Cadena vacía borra el teléfono; null no modifica el valor guardado. */
    private String telefono;
    /** Si va vacío/null, no cambia la contraseña. */
    private String nuevaPassword;
}
