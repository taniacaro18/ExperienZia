package com.experienzia.entity;

// Estado de la cuenta en usuarios — si puede loguearse o está bloqueado/pendiente.
public enum Estado {
    ACTIVO,     // normal, puede entrar
    PENDIENTE,  // recién registrado (ej. organizador esperando que lo aprueben)
    RECHAZADO,  // el admin dijo que no
    INACTIVO    // deshabilitado
}
