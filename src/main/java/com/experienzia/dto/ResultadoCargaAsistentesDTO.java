package com.experienzia.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

// Devuelvo esto al terminar la carga masiva — resumen de éxitos, omitidos y mensajes de error.
@Data
@NoArgsConstructor

public class ResultadoCargaAsistentesDTO {

    private int cuentasNuevasCreadas;

    private int inscripcionesRegistradas;

    private int filasOmitidasDuplicadoUOtros;
    
    private List<String> errores = new ArrayList<>();
}
