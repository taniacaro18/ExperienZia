package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para rechazar pago. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class RechazarPagoDTO {
    /** Dato del campo motivo */
    private String motivo;
    /** Dato del campo aprobador id */
    private Long aprobadorId;
}
