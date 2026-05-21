package com.experienzia.entity;

/**
 * Enum (no es tabla): indica qué tarea hace un STAFF en un evento concreto.
 * Se guarda como texto en {@link StaffEventoAsignacion}.
 */
public enum FuncionStaff {
    /** Escanear el código QR del asistente al entrar. */
    CHECK_IN_QR,
    /** Registrar entrada a mano sin QR. */
    CHECK_IN_MANUAL,
    /** Marcar la salida (check-out) del asistente. */
    REGISTRO_SALIDA,
    /** Varias tareas o sin rol fijo; valor por defecto. */
    GENERAL
}
