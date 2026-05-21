package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad JPA del pago que hace el ORGANIZADOR a la plataforma para publicar o ampliar un evento.
 * No es el pago del asistente al evento: es la tarifa de ExperienZia (aprox. precioPorHora × duración).
 * Tabla {@code pagos}; se relaciona con {@link Evento} y {@link Usuario}.
 */
@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Evento al que corresponde este pago de activación. */
    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    /** Relación JPA: un evento puede tener varios pagos en el tiempo (suplementos). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_pago_evento"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Evento evento;

    /** Quién pagó (debe ser el organizador dueño del evento). */
    @Column(name = "organizador_id", nullable = false)
    private Long organizadorId;

    /** Relación JPA al usuario organizador que subió el comprobante. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_pago_organizador"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario organizador;

    /** Null cuando el organizador aún no subió comprobante o tras cambio de tarifa / suplemento. */
    @Column(name = "comprobante_url", length = 500, nullable = true)
    private String comprobanteUrl;

    /** Monto del pago en COP (precioPorHora * duracionHoras del evento). */
    @Column(nullable = false)
    private double monto;

    /**
     * Si no es null, el pago en PENDIENTE es un complemento: {@link #monto} es solo el incremento
     * y este campo guarda el monto ya aprobado previamente (se suman al aprobar el complemento).
     */
    @Column(name = "saldo_aprobado_previo")
    private Double saldoAprobadoPrevio;

    /** PENDIENTE hasta que un admin apruebe o rechace el comprobante. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    /** Fecha en que se registró el pago en el sistema. */
    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "motivo_rechazo", length = 2000)
    private String motivoRechazo;

    /** ID del administrador que resolvió el pago (si ya se revisó). */
    @Column(name = "aprobador_id")
    private Long aprobadorId;

    /** Relación JPA al admin que aprobó o rechazó. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobador_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_pago_aprobador"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario aprobador;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
}
