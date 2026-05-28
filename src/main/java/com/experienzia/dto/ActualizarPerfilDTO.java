package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body del PATCH de perfil: teléfono y opcionalmente contraseña nueva (usuario ya logueado).
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ActualizarPerfilDTO {
    private String telefono;
    private String nuevaPassword;
}
