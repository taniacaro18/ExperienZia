package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad JPA de un aviso dentro de la app (mensaje en bandeja de notificaciones).
 * Cada fila es un mensaje para un {@link Usuario} concreto.
 */
@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** A quién va dirigida la notificación. */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /** Relación JPA al destinatario (muchos avisos pueden tener el mismo usuario). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_notificacion_usuario"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    /** Texto que ve el usuario en la campana o listado. */
    @Column(nullable = false, length = 500)
    private String mensaje;

    /** INFO, ALERTA o ERROR para mostrar distinto estilo o prioridad. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacion tipo;

    /** false = sin leer; true = el usuario ya la abrió o marcó como leída. */
    @Column(nullable = false)
    private boolean leida;

    /** Momento en que se creó la notificación. */
    @Column(nullable = false)
    private LocalDateTime fecha;
}
