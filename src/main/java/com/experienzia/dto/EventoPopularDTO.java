package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Un ítem del top de eventos populares que armo para el dashboard admin.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class EventoPopularDTO {

    private Long eventoId;

    private String nombre;
    
    private Long totalInscritos;
}
