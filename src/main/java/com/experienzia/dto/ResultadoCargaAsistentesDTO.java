package com.experienzia.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class ResultadoCargaAsistentesDTO {

    private int cuentasNuevasCreadas;

    private int inscripcionesRegistradas;

    private int filasOmitidasDuplicadoUOtros;
    
    private List<String> errores = new ArrayList<>();
}
