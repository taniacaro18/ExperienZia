package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Tres contadores globales que a veces devuelvo en endpoints de resumen rápido.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ResumenDTO {

    private Long totalUsuarios;

    private Long totalEventos;
    
    private Long totalInscripciones;
}
