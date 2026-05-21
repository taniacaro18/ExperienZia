package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para carga asistentes manual. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class CargaAsistentesManualDTO {
    /** Dato del campo organizador id */
    private Long organizadorId;
    /** Dato del campo filas */
    private List<FilaAsistenteCargaDTO> filas;
}
