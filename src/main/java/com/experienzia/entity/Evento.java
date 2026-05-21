package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad JPA de un evento (conferencia, taller, etc.) creado por un organizador.
 * La tabla {@code eventos} guarda fechas, aforo, costo y el ciclo de vida (aprobación, activo, cancelado…).
 */
@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Título del evento que ve el usuario en listados. */
    @Column(nullable = false, length = 200)
    private String nombre;

    /** Texto largo con detalles del evento. */
    @Column(length = 1000)
    private String descripcion;

    /** Fecha y hora de inicio del evento. */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Fecha y hora de fin; sirve para calcular duración y cerrar el evento. */
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    /** Lugar o sala donde se realiza (se usa para evitar choques de reserva). */
    @Column(length = 200)
    private String ubicacion;

    /** Público (cualquiera puede ver) o privado (acceso restringido). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEvento tipoEvento;

    /** Estado actual del evento en el flujo de negocio (pendiente, activo, cancelado…). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoEvento estado;

    /**
     * Cuando el evento entra en {@link EstadoEvento#PENDIENTE_REVISION},
     * {@link EstadoEvento#PENDIENTE_SUPLEMENTO} o {@link EstadoEvento#PENDIENTE_CANCELACION},
     * guarda el estado anterior para restaurarlo al aprobar/rechazar según reglas.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_previo_revision", length = 32)
    private EstadoEvento estadoPrevioRevision;

    /** Cupo máximo de asistentes permitidos. */
    @Column(nullable = false)
    private int aforoMaximo;

    /** Cuántas personas están inscritas ahora (para no pasarse del máximo). */
    @Column(nullable = false)
    private int aforoActual;

    /** Precio o tarifa del evento para el asistente (en la moneda que use la app). */
    @Column(nullable = false)
    private double costo;

    /** ID del usuario organizador dueño del evento (se guarda como número, no como objeto). */
    @Column(name = "organizador_id", nullable = false)
    private Long organizadorId;

    /**
     * Relación JPA al {@link Usuario} organizador.
     * Solo lectura en JPA: los cambios de dueño van por {@link #organizadorId}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_evento_organizador"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario organizador;

    /** URL o ruta de la imagen de portada del evento. */
    private String imagen;

    /** Etiqueta de categoría (deportes, cultura, etc.) para filtrar. */
    @Column(length = 100)
    private String categoria;

    /** Horas de duración; se usa junto con el pago a la plataforma. */
    @Column(name = "duracion_horas")
    private Integer duracionHoras;

    @Column(name = "motivo_rechazo", length = 2000)
    private String motivoRechazo;

    @Column(name = "motivo_cancelacion", length = 2000)
    private String motivoCancelacion;

    /**
     * Texto breve para el administrador: qué cambió en la última edición que requirió re-aprobación.
     * Se limpia al aprobar el evento.
     */
    @Column(name = "resumen_solicitud_edicion", length = 2000)
    private String resumenSolicitudEdicion;
}
