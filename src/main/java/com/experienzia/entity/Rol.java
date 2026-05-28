package com.experienzia.entity;

// No es tabla: lo persisto como STRING en usuarios. Define qué pantallas y endpoints puede usar cada quien.
public enum Rol {
    ASISTENTE,   // se inscribe y va a eventos
    ORGANIZADOR, // crea y administra sus eventos
    STAFF,       // ayuda en puerta en eventos de su organizador
    ADMIN        // aprueba eventos, pagos, usuarios, etc.
}
