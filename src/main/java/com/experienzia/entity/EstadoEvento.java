package com.experienzia.entity;

/**
 * Enum del ciclo de vida de un {@link Evento}. Controla qué acciones están permitidas.
 */
public enum EstadoEvento {
    /** Solicitud inicial de evento nuevo (sin pago aún o flujo clásico). */
    PENDIENTE,
    /** Admin aceptó el evento pero puede faltar activación/pago. */
    APROBADO,
    /** Admin rechazó la publicación. */
    RECHAZADO,
    /** Evento visible y abierto a inscripciones (según reglas). */
    ACTIVO,
    /** El evento ya ocurrió (ventana de inicio–fin cerrada). */
    FINALIZADO,
    /** Evento cancelado y no se realizará. */
    CANCELADO,
    /**
     * Edición de un evento ya activo/aprobado que no implica suplemento de pago por horas
     * (o reducción de horas con penalización): requiere aprobación administrativa.
     */
    PENDIENTE_REVISION,
    /** Aumento de horas con pago ya aprobado: debe pagarse solo el excedente y aprobarse el comprobante. */
    PENDIENTE_SUPLEMENTO,
    /** Solicitud de cancelación por el organizador; el admin aprueba o rechaza. */
    PENDIENTE_CANCELACION
}
