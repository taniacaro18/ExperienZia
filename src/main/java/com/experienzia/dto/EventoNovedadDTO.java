package com.experienzia.dto;

import java.time.LocalDateTime;

import com.experienzia.entity.EstadoNovedadEvento;
import com.experienzia.entity.TipoNovedadEvento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Expongo una fila de evento_novedades al admin/organizador — historial de cambios pendientes o resueltos.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class EventoNovedadDTO {

    private Long id;

    private Long eventoId;

    private Long usuarioSolicitanteId;

    private TipoNovedadEvento tipo;

    private EstadoNovedadEvento estado;

    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaResolucion;

    private String motivoResolucion;
    
    private String detalleJson;
}
