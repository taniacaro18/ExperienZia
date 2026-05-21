package com.experienzia.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
/**
 * Objeto de transferencia (DTO) para resultado carga asistentes. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class ResultadoCargaAsistentesDTO {
    /** Dato del campo cuentas nuevas creadas */
    private int cuentasNuevasCreadas;
    /** Dato del campo inscripciones registradas */
    private int inscripcionesRegistradas;
    /** Campo `filasOmitidasDuplicadoUOtros` (filas omitidas duplicado u otros) */
    private int filasOmitidasDuplicadoUOtros;
    private List<String> errores = new ArrayList<>();
}
