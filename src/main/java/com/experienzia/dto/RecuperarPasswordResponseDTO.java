package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para recuperar password response. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class RecuperarPasswordResponseDTO {
    /** Id del usuario relacionado */
    private Long usuarioId;
    /** Correo electrónico del usuario */
    private String email;
    /** Campo `passwordTemporal` (password temporal) */
    private String passwordTemporal;
    /** Texto que se muestra al usuario */
    private String mensaje;
}
