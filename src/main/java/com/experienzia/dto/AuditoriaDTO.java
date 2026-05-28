package com.experienzia.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo serializo al front en el listado de auditoría del admin (viene de la tabla auditorias).
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AuditoriaDTO {

    private Long id;

    private Long usuarioId;

    private String accion;

    private String entidad;

    private Long entidadId;

    private LocalDateTime fecha;
    
    private String direccionIp;
}
