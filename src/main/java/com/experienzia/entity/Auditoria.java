package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad JPA de registro de auditoría: quién hizo qué y cuándo en el sistema.
 * Sirve para trazabilidad (seguridad, soporte, cumplimiento). Tabla {@code auditorias}.
 */
@Entity
@Table(name = "auditorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que ejecutó la acción (puede ser null si fue el sistema). */
    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Relación JPA opcional al usuario que actuó. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_auditoria_usuario"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    /** Nombre de la operación, por ejemplo CREAR_EVENTO o APROBAR_PAGO. */
    @Column(nullable = false)
    private String accion;

    /** Tipo de tabla/entidad afectada, por ejemplo Evento o Usuario. */
    @Column(nullable = false)
    private String entidad;

    /** ID del registro concreto que se modificó (si aplica). */
    @Column(name = "entidad_id")
    private Long entidadId;

    /** Fecha y hora del evento auditado. */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Dirección IP del cliente (IPv4/IPv6). ROOM_911 / trazabilidad. */
    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;
}
