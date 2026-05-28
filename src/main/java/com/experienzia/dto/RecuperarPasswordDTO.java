package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body de recuperar contraseña: cruzo email + documento antes de generar clave temporal.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RecuperarPasswordDTO {

    private String email;
    
    private String numeroDocumento;
}
