package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para recuperar password. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class RecuperarPasswordDTO {
    /** Correo electrónico del usuario */
    private String email;
    /** Dato del campo numero documento */
    private String numeroDocumento;
}
