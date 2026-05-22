package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CurvaIngresoPuntoDTO {

    private int hora;

    private long ingresos;
    
    private long salidas;
}
