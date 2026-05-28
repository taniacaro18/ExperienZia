package com.experienzia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// Entidad JPA: tabla certificados. Lo genero cuando el asistente asistió — el front/PDF usa el codigoUnico para validar.
@Entity
@Table(name = "certificados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_certificado_usuario"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_certificado_evento"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Evento evento;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "codigo_unico", nullable = false, unique = true, length = 100)
    private String codigoUnico;
}
