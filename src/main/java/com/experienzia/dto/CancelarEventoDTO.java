package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body de cancelación: valido que sea el organizador dueño y guardo el motivo en el evento.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CancelarEventoDTO {

    private Long organizadorId;
    
    private String motivo;
}
