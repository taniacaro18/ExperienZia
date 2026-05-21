package com.experienzia.entity;

/**
 * Enum de visibilidad o acceso del {@link Evento}.
 */
public enum TipoEvento {
    /** Visible para todos los asistentes según reglas de listado. */
    PUBLICO,
    /** Acceso restringido (invitación, código, etc.). */
    PRIVADO
}
