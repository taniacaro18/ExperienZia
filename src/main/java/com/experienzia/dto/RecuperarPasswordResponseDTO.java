package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta que devuelvo al front tras validar — incluye clave temporal (en prod habría que mandarla por correo).
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RecuperarPasswordResponseDTO {

    private Long usuarioId;

    private String email;

    private String passwordTemporal;
    
    private String mensaje;
}
