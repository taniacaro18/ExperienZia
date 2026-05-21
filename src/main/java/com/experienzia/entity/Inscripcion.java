package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad JPA que une a un {@link Usuario} con un {@link Evento}: la inscripción o reserva de plaza.
 * Una fila en {@code inscripciones} = "esta persona va a este evento".
 */
@Entity
@Table(name = "inscripciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del asistente inscrito. */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /** Relación JPA al usuario inscrito (carga perezosa, solo lectura vía FK). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_inscripcion_usuario"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    /** ID del evento al que se inscribió la persona. */
    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    /** Relación JPA al evento (muchos inscritos pueden apuntarse al mismo evento). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_inscripcion_evento"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Evento evento;

    /** Momento en que el usuario confirmó la inscripción. */
    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    /** INSCRITO, CANCELADO o ASISTIO según el flujo del día del evento. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoInscripcion estado;

    /** Hora en que el staff marcó la entrada (check-in). */
    @Column(name = "fecha_check_in")
    private LocalDateTime fechaCheckIn;

    /** Hora en que marcó la salida (check-out). */
    @Column(name = "fecha_check_out")
    private LocalDateTime fechaCheckOut;

    /** Código único del QR para validar entrada en puerta. */
    @Column(name = "codigo_qr", unique = true, length = 64)
    private String codigoQR;
}
