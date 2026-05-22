package com.experienzia.dto;

import java.time.LocalDateTime;

import com.experienzia.entity.EstadoEvento;

import lombok.Data;

@Data

public class FranjaOcupacionSalonDTO {

    private Long eventoId;

    private String nombreEvento;

    private EstadoEvento estado;

    private LocalDateTime inicio;

    private LocalDateTime fin;
    
    private String nombreOrganizador;
}
