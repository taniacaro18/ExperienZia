package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para asignar staff. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class AsignarStaffDTO {
    /** Dato del campo organizador id */
    private Long organizadorId;
    /** Dato del campo staff usuario id */
    private Long staffUsuarioId;
    /**
     * Función dentro del evento: CHECK_IN_QR, CHECK_IN_MANUAL, REGISTRO_SALIDA o GENERAL.
     * Si no se envía, se asume GENERAL.
     */
    private String funcion;
}
