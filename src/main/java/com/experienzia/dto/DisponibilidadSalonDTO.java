package com.experienzia.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data

public class DisponibilidadSalonDTO {

    private String ubicacion;
    private LocalDateTime desde;

    private LocalDateTime hasta;

    private Boolean propuestaDisponible;

    private String mensajePropuesta;
    
    private List<FranjaOcupacionSalonDTO> ocupaciones = new ArrayList<>();
}
