package com.experienzia.dto;

import com.experienzia.entity.EstadoNovedadEvento;
import com.experienzia.entity.TipoNovedadEvento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para evento novedad. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class EventoNovedadDTO {
    /** Identificador único en la base de datos */
    private Long id;
    /** Id del evento al que pertenece */
    private Long eventoId;
    /** Campo `usuarioSolicitanteId` (usuario solicitante id) */
    private Long usuarioSolicitanteId;
    /** Tipo o categoría según el contexto */
    private TipoNovedadEvento tipo;
    /** Estado actual (ACTIVO, PENDIENTE, etc.) */
    private EstadoNovedadEvento estado;
    /** Campo `fechaSolicitud` (fecha solicitud) */
    private LocalDateTime fechaSolicitud;
    /** Dato del campo fecha resolucion */
    private LocalDateTime fechaResolucion;
    /** Dato del campo motivo resolucion */
    private String motivoResolucion;
    /** Campo `detalleJson` (detalle json) */
    private String detalleJson;
}
