package com.experienzia.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class EventoStaffDTO {

    private Long asignacionId;

    private Long eventoId;

    private String nombreEvento;

    private String descripcion;

    private LocalDateTime fechaEvento;

    private String ubicacion;

    private String tipoEvento;

    private String estadoEvento;

    private String categoria;

    private Integer aforoMaximo;

    private Integer aforoActual;

    private Long organizadorId;
    
    private String funcion;
}
