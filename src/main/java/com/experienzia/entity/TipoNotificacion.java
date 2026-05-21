package com.experienzia.entity;

/**
 * Enum del tipo visual o de prioridad de una {@link Notificacion}.
 */
public enum TipoNotificacion {
    /** Mensaje informativo normal. */
    INFO,
    /** Aviso importante que requiere atención. */
    ALERTA,
    /** Algo falló o es crítico. */
    ERROR
}
