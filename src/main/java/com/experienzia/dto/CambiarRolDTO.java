package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body que recibo en el endpoint de admin para cambiar el rol de un usuario.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CambiarRolDTO {
    private String rol;
}
