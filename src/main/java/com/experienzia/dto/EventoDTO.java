package com.experienzia.dto;

import java.time.LocalDateTime;

import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.TipoEvento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// El evento completo en JSON — creación, edición y detalle en catálogo/admin (mapeo desde entidad + extras).
@Data
@NoArgsConstructor
@AllArgsConstructor

public class EventoDTO {

    private Long id;

    private String nombre;

    private String descripcion;

    // Fechas del evento (inicio y fin para duración y sala)
    private LocalDateTime fecha;

    private LocalDateTime fechaFin;

    private String ubicacion;

    private TipoEvento tipoEvento;

    private EstadoEvento estado;

    // Aforo y precio
    private Integer aforoMaximo;

    private Integer aforoActual;

    private Double costo;

    // Datos del organizador (los relleno en el servicio para no ir al front a otra petición)
    private Long organizadorId;

    private String organizadorNombre;

    private String organizadorEmail;

    private String imagen;

    private String categoria;

    private Integer duracionHoras;

    // Motivos y flujo de revisión admin
    private String motivoRechazo;

    private String motivoCancelacion;

    private String resumenSolicitudEdicion;

    private EstadoEvento estadoPrevioRevision;
    
    private String alertaNegocio;
}
