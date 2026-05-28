package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// Entidad JPA: tabla evento_novedades. Historial de cambios que pide el organizador y el admin aprueba o tumba.
@Entity
@Table(name = "evento_novedades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoNovedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

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

    @Column(name = "usuario_solicitante_id", nullable = false)
    private Long usuarioSolicitanteId;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNovedadEvento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoNovedadEvento estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "motivo_resolucion", length = 2000)
    private String motivoResolucion;

    // Guardo el diff en JSON para no perder qué pidió exactamente el organizador
    @Column(name = "detalle_json", columnDefinition = "TEXT")
    private String detalleJson;
}
