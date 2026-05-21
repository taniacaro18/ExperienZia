package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para resumen. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class ResumenDTO {
    /** Dato del campo total usuarios */
    private Long totalUsuarios;
    /** Dato del campo total eventos */
    private Long totalEventos;
    /** Campo `totalInscripciones` (total inscripciones) */
    private Long totalInscripciones;
}
