package com.experienzia.entity;

// Estado de una fila en inscripciones — desde que reserva hasta que asiste o cancela.
public enum EstadoInscripcion {
    INSCRITO,   // tiene cupo
    CANCELADO,  // ya no va
    ASISTIO     // hizo check-in (o lo marqué como asistió)
}
