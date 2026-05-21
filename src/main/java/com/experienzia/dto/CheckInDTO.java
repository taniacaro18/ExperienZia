package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para check in. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class CheckInDTO {
    /** Dato del campo staff usuario id */
    private Long staffUsuarioId;
    /** Dato del campo codigo q r */
    private String codigoQR;
    /** Id del evento al que pertenece */
    private Long eventoId;
}
