package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// Entidad JPA: tabla usuarios en Postgres. Acá persisto a quien entra a la app (asistente, organizador, staff o admin).
@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    // Login: el email es único en toda la BD
    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 200)
    private String password;

    @Column(unique = true, length = 50)
    private String telefono;

    // Documento colombiano (CC, CE, etc.)
    @Column(length = 30)
    private String tipoDocumento;

    @Column(unique = true, length = 50)
    private String numeroDocumento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Estado estado;

    // Si el rol es STAFF, acá va el id del organizador que lo creó (null para los demás)
    @Column(name = "organizador_id")
    private Long organizadorId;

    // Relación ManyToOne al organizador: LAZY para no cargar de más; el id real lo manejo en organizadorId arriba
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
