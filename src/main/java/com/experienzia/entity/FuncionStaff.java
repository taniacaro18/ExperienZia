package com.experienzia.entity;

// Qué hace el staff en puerta — va en staff_evento_asignaciones como STRING, no es tabla aparte.
public enum FuncionStaff {
    CHECK_IN_QR,      // escanea QR
    CHECK_IN_MANUAL,  // marca entrada a mano
    REGISTRO_SALIDA,  // check-out
    GENERAL           // de todo un poco (default)
}
