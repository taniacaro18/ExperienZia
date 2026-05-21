package com.experienzia.entity;

/**
 * Enum: si una solicitud en {@link EventoNovedad} ya fue resuelta por el administrador.
 */
public enum EstadoNovedadEvento {
    /** El admin aún no decidió. */
    PENDIENTE,
    /** Se aplicó el cambio solicitado. */
    APROBADO,
    /** No se aplicó el cambio. */
    RECHAZADO
}
