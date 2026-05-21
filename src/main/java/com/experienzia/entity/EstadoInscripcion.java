package com.experienzia.entity;

/**
 * Enum del estado de una {@link Inscripcion} de un asistente a un evento.
 */
public enum EstadoInscripcion {
    /** Tiene plaza reservada. */
    INSCRITO,
    /** Dejó de asistir o anuló la inscripción. */
    CANCELADO,
    /** Entró al evento (check-in realizado). */
    ASISTIO
}
