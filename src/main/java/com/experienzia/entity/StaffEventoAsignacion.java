package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entidad JPA que asigna un usuario STAFF a un {@link Evento} con una {@link FuncionStaff}.
 * Tabla intermedia: un staff puede trabajar en varios eventos, y un evento puede tener varios staff.
 * La restricción única evita duplicar la misma pareja staff+evento.
 */
@Entity
@Table(name = "staff_evento_asignaciones",
        uniqueConstraints = @UniqueConstraint(name = "uk_staff_evento", columnNames = {"staff_usuario_id", "evento_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffEventoAsignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del usuario con rol STAFF asignado al evento. */
    @Column(name = "staff_usuario_id", nullable = false)
    private Long staffUsuarioId;

    /** Relación JPA al miembro del equipo (usuario STAFF). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_usuario_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_asignacion_staff"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario staff;

    /** ID del evento donde trabaja ese staff. */
    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    /** Relación JPA al evento asignado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_asignacion_evento"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Evento evento;

    /** Tarea concreta del staff en puerta: QR, manual, salida o general. */
    @Enumerated(EnumType.STRING)
    @Column(name = "funcion", length = 30, nullable = false)
    private FuncionStaff funcion = FuncionStaff.GENERAL;

    /** Constructor rápido: staff + evento con función GENERAL por defecto. */
    public StaffEventoAsignacion(Long staffUsuarioId, Long eventoId) {
        this.staffUsuarioId = staffUsuarioId;
        this.eventoId = eventoId;
        this.funcion = FuncionStaff.GENERAL;
    }

    public StaffEventoAsignacion(Long staffUsuarioId, Long eventoId, FuncionStaff funcion) {
        this.staffUsuarioId = staffUsuarioId;
        this.eventoId = eventoId;
        this.funcion = funcion == null ? FuncionStaff.GENERAL : funcion;
    }
}
