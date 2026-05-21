package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entidad JPA del certificado de asistencia que recibe un usuario tras un evento.
 * Une {@link Usuario} + {@link Evento} con un código único para verificar autenticidad.
 */
@Entity
@Table(name = "certificados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Asistente que obtuvo el certificado. */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /** Relación JPA al titular del certificado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_certificado_usuario"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    /** Evento en el que participó y por el que se emitió el certificado. */
    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    /** Relación JPA al evento asociado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_certificado_evento"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Evento evento;

    /** Cuándo el sistema generó el certificado. */
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    /** Código público para validar el certificado (como un número de serie). */
    @Column(name = "codigo_unico", nullable = false, unique = true, length = 100)
    private String codigoUnico;
}
