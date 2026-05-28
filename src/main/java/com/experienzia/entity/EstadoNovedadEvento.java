package com.experienzia.entity;

// Si la solicitud en evento_novedades ya la resolvió el admin o sigue en cola.
public enum EstadoNovedadEvento {
    PENDIENTE,
    APROBADO,
    RECHAZADO
}
