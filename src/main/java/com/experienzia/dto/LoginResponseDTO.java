package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Respuesta del login: usuario y token JWT.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class LoginResponseDTO {

    /** JWT para Authorization: Bearer … */
    private String accessToken;

    /** Dato del campo usuario */
    private UsuarioDTO usuario;
}
