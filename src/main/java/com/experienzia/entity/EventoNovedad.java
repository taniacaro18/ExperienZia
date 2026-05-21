package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad JPA del historial de cambios solicitados sobre un {@link Evento}.
 * Cada fila es una "novedad" (editar datos, cambiar horas, pedir cancelación) que el admin puede aprobar o rechazar.
 * Tabla {@code evento_novedades}.
 */
@Entity
@Table(name = "evento_novedades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoNovedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Evento al que aplica esta solicitud de cambio. */
    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    /** Relación JPA al evento (varias novedades pueden existir para el mismo evento). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "evento_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_evento_novedad_evento"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Evento evento;

    /** Organizador que envió la solicitud. */
    @Column(name = "usuario_solicitante_id", nullable = false)
    private Long usuarioSolicitanteId;

    /** Relación JPA al usuario solicitante (normalmente rol ORGANIZADOR). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuario_solicitante_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_evento_novedad_usuario"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuarioSolicitante;

    /** Qué tipo de cambio pidió (edición, más horas, cancelación…). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNovedadEvento tipo;

    /** Si la solicitud sigue PENDIENTE o ya fue APROBADA/RECHAZADA. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoNovedadEvento estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    /** Cuándo el administrador cerró la solicitud. */
    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    /** Comentario del admin al aprobar o rechazar. */
    @Column(name = "motivo_resolucion", length = 2000)
    private String motivoResolucion;

    /** JSON con el detalle del cambio propuesto (para no perder datos en el historial). */
    @Column(name = "detalle_json", columnDefinition = "TEXT")
    private String detalleJson;
}
