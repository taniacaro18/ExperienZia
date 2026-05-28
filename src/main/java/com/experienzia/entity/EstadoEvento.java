package com.experienzia.entity;

// Ciclo de vida del evento en BD — con esto valido si se puede editar, inscribir o cancelar.
public enum EstadoEvento {
    // Flujo inicial y publicación
    PENDIENTE,
    APROBADO,
    RECHAZADO,
    ACTIVO,
    FINALIZADO,
    CANCELADO,
    // Estados intermedios cuando el organizador pide cambios con evento ya vivo
    PENDIENTE_REVISION,      // edición que no es solo suplemento de horas
    PENDIENTE_SUPLEMENTO,      // aumentó horas y debe pagar el excedente
    PENDIENTE_CANCELACION      // pidió cancelar, espera el admin
}
