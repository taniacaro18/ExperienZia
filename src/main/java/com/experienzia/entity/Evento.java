package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// Entidad JPA: tabla eventos. Es el corazón del negocio — lo que crea el organizador y el admin aprueba o rechaza.
@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    // Ventana de tiempo del evento (sirve para sala, reportes y cerrar automático)
    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(length = 200)
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEvento tipoEvento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoEvento estado;

    // Cuando queda en PENDIENTE_REVISION / SUPLEMENTO / CANCELACION guardo el estado anterior para volver atrás si toca
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_previo_revision", length = 32)
    private EstadoEvento estadoPrevioRevision;

    // Cupos
    @Column(nullable = false)
    private int aforoMaximo;

    @Column(nullable = false)
    private int aforoActual;

    @Column(nullable = false)
    private double costo;

    @Column(name = "organizador_id", nullable = false)
    private Long organizadorId;

    // Dueño del evento — insertable/updatable false porque el FK lo escribo con organizadorId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_evento_organizador"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario organizador;

    // Presentación en el catálogo del front
    private String imagen;

    @Column(length = 100)
    private String categoria;

    @Column(name = "duracion_horas")
    private Integer duracionHoras;

    // Textos que deja el admin u organizador en rechazos/cancelaciones/ediciones
    @Column(name = "motivo_rechazo", length = 2000)
    private String motivoRechazo;

    @Column(name = "motivo_cancelacion", length = 2000)
    private String motivoCancelacion;

    // Resumen para el admin cuando el organizador editó algo que requiere otra vuelta de aprobación
    @Column(name = "resumen_solicitud_edicion", length = 2000)
    private String resumenSolicitudEdicion;
}
