package com.experienzia.entity;

/**
 * Enum del estado de la cuenta de un {@link Usuario} (si puede entrar o no).
 */
public enum Estado {
    /** Cuenta normal y operativa. */
    ACTIVO,
    /** Esperando aprobación (por ejemplo organizador nuevo). */
    PENDIENTE,
    /** Registro denegado por un admin. */
    RECHAZADO,
    /** Cuenta deshabilitada o bloqueada. */
    INACTIVO
}
