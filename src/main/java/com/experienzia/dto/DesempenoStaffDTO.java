package com.experienzia.dto;

import lombok.Data;

@Data
/**
 * Objeto de transferencia (DTO) para desempeno staff. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class DesempenoStaffDTO {
    /** Dato del campo staff usuario id */
    private Long staffUsuarioId;
    /** Nombre completo de la persona */
    private String nombre;
    /** Campo `funcion` (funcion) */
    private String funcion;
    /** Dato del campo check ins registrados */
    private long checkInsRegistrados;
    /** Dato del campo check outs registrados */
    private long checkOutsRegistrados;
    /** Campo `checkInsPorQR` (check ins por q r) */
    private long checkInsPorQR;
    /** Dato del campo check ins manuales */
    private long checkInsManuales;
}
