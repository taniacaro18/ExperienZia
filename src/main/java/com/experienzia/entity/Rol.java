package com.experienzia.entity;

/**
 * Enum de roles de usuario. No es una tabla: JPA lo guarda como texto en {@link Usuario#rol}.
 */
public enum Rol {
    /** Persona que se inscribe y asiste a eventos. */
    ASISTENTE,
    /** Crea y gestiona sus propios eventos. */
    ORGANIZADOR,
    /** Ayuda en puerta (check-in/out) en eventos de un organizador. */
    STAFF,
    /** Administrador de la plataforma (aprueba eventos, pagos, etc.). */
    ADMIN
}
