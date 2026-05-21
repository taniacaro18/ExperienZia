package com.experienzia.entity;

/**
 * Enum del estado de un {@link Pago} de tarifa de plataforma hecho por el organizador.
 */
public enum EstadoPago {
    /** Comprobante subido, esperando revisión del admin. */
    PENDIENTE,
    /** Admin validó el pago; el evento puede avanzar. */
    APROBADO,
    /** Admin rechazó el comprobante. */
    RECHAZADO
}
