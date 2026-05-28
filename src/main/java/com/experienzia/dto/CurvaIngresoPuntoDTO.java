package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Un punto hora a hora de la curva de ingresos/salidas en el reporte avanzado.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CurvaIngresoPuntoDTO {

    private int hora;

    private long ingresos;
    
    private long salidas;
}
