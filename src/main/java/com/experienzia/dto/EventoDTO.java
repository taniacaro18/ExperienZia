package com.experienzia.dto;

import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.TipoEvento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Datos de un evento (título, fechas, aforo, etc.).
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class EventoDTO {
    /** Identificador único en la base de datos */
    private Long id;
    /** Nombre completo de la persona */
    private String nombre;
    /** Texto descriptivo */
    private String descripcion;
    /** Dato del campo fecha */
    private LocalDateTime fecha;
    /** Dato del campo fecha fin */
    private LocalDateTime fechaFin;
    /** Campo `ubicacion` (ubicacion) */
    private String ubicacion;
    /** Dato del campo tipo evento */
    private TipoEvento tipoEvento;
    /** Estado actual (ACTIVO, PENDIENTE, etc.) */
    private EstadoEvento estado;
    /** Campo `aforoMaximo` (aforo maximo) */
    private Integer aforoMaximo;
    /** Dato del campo aforo actual */
    private Integer aforoActual;
    /** Dato del campo costo */
    private Double costo;
    /** Campo `organizadorId` (organizador id) */
    private Long organizadorId;
    /** Nombre del usuario organizador (contacto para asistentes inscritos). */
    private String organizadorNombre;
    /** Correo del organizador (solo cuando el API no es catálogo público anonimizado). */
    private String organizadorEmail;
    /** Dato del campo imagen */
    private String imagen;
    /** Dato del campo categoria */
    private String categoria;
    /** Campo `duracionHoras` (duracion horas) */
    private Integer duracionHoras;
    /** Dato del campo motivo rechazo */
    private String motivoRechazo;
    /** Dato del campo motivo cancelacion */
    private String motivoCancelacion;
    /** Qué pidió cambiar el organizador (solo cuando el evento queda PENDIENTE por edición). */
    private String resumenSolicitudEdicion;
    /** Estado previo cuando el evento está en revisión / suplemento / cancelación pendiente. */
    private EstadoEvento estadoPrevioRevision;
    /** Mensaje de negocio para el organizador (no persistido; solo respuesta API). */
    private String alertaNegocio;
}
