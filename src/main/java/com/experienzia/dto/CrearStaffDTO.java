package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para crear staff. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class CrearStaffDTO {
    /** Nombre completo de la persona */
    private String nombre;
    /** Correo electrónico del usuario */
    private String email;
    /** Contraseña (en el servidor va encriptada) */
    private String password;
    /** Número de teléfono de contacto */
    private String telefono;
    /** Dato del campo tipo documento */
    private String tipoDocumento;
    /** Campo `numeroDocumento` (numero documento) */
    private String numeroDocumento;
    /** Dato del campo organizador id */
    private Long organizadorId;
}
