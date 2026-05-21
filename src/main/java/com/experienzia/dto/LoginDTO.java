package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Datos para iniciar sesión (email y contraseña).
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class LoginDTO {
    /** Correo electrónico del usuario */
    private String email;
    /** Contraseña (en el servidor va encriptada) */
    private String password;
}
