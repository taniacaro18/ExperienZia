package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Entidad JPA que representa a una persona registrada en ExperienZia.
 * Cada fila de la tabla {@code usuarios} es un usuario que puede iniciar sesión:
 * asistente, organizador, staff o administrador.
 */
@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    /** Identificador único generado automáticamente por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre completo o de perfil del usuario. */
    @Column(nullable = false, length = 150)
    private String nombre;

    /** Correo usado para login; no puede repetirse entre usuarios. */
    @Column(unique = true, nullable = false, length = 150)
    private String email;

    /** Contraseña guardada (en la app se hashea antes de guardar). */
    @Column(nullable = false, length = 200)
    private String password;

    /** Teléfono opcional, también único si se informa. */
    @Column(unique = true, length = 50)
    private String telefono;

    /** Tipo de documento, por ejemplo CC o CE. */
    @Column(length = 30)
    private String tipoDocumento;

    /** Número de documento de identidad, único en el sistema. */
    @Column(unique = true, length = 50)
    private String numeroDocumento;

    /** Rol del usuario: qué puede hacer en la plataforma (ver enum {@link Rol}). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol;

    /** Si la cuenta está activa, pendiente de aprobación, rechazada, etc. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Estado estado;

    /** ID del organizador que creó este STAFF. Null para otros roles. */
    @Column(name = "organizador_id")
    private Long organizadorId;

    /**
     * Relación JPA {@code ManyToOne}: muchos usuarios STAFF pueden pertenecer a un mismo organizador.
     * {@code LAZY} = no carga el organizador hasta que lo uses.
     * {@code insertable/updatable false} = el ID real se guarda en {@link #organizadorId}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_usuario_organizador"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario organizador;
}
