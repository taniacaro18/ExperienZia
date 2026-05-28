package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta del POST /login — mando el token JWT y el usuario sin password para el localStorage de Angular.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class LoginResponseDTO {

    private String accessToken;

    private UsuarioDTO usuario;
}
