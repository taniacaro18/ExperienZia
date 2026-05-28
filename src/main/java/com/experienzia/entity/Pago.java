package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// Entidad JPA: tabla pagos. Ojo: es lo que el ORGANIZADOR le paga a ExperienZia por publicar el evento, no el boleto del asistente.
@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_pago_evento"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Evento evento;

    @Column(name = "organizador_id", nullable = false)
    private Long organizadorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_pago_organizador"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario organizador;

    // URL del comprobante que sube el organizador (puede quedar null si aún no paga o hubo cambio de tarifa)
    @Column(name = "comprobante_url", length = 500, nullable = true)
    private String comprobanteUrl;

    @Column(nullable = false)
    private double monto;

    // Si es complemento por más horas: monto es solo el delta y acá guardo lo que ya estaba aprobado
    @Column(name = "saldo_aprobado_previo")
    private Double saldoAprobadoPrevio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "motivo_rechazo", length = 2000)
    private String motivoRechazo;

    @Column(name = "aprobador_id")
    private Long aprobadorId;

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
