package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lleva los datos de un usuario entre el front y el back.
 * Se usa al registrarse, al devolver el perfil y dentro del login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Información de un usuario para crear o mostrar.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class UsuarioDTO {

    /** Identificador del usuario en la base de datos. */
    private Long id;
    /** Nombre completo o de pantalla. */
    private String nombre;
    /** Correo con el que entra a la app. */
    private String email;
    /** Contraseña (solo al crear o cambiar; no siempre se devuelve). */
    private String password;
    /** Teléfono de contacto. */
    private String telefono;
    /** Tipo de documento, por ejemplo CC o CE. */
    private String tipoDocumento;
    /** Número del documento de identidad. */
    private String numeroDocumento;
    /** Rol en el sistema: ASISTENTE, ORGANIZADOR, STAFF o ADMIN. */
    private String rol;
    /** Si la cuenta está ACTIVO, PENDIENTE, RECHAZADO o INACTIVO. */
    private String estado;
    /** Organizador al que pertenece el staff (solo cuando rol = STAFF). */
    private Long organizadorId;
    /** En el registro público indica si quiere ser ASISTENTE u ORGANIZADOR. */
    private String tipo;
}
