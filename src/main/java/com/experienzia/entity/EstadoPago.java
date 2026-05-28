package com.experienzia.entity;

// Estado del comprobante que sube el organizador en pagos — el admin lo mueve de PENDIENTE.
public enum EstadoPago {
    PENDIENTE,  // esperando revisión
    APROBADO,   // listo, el evento puede seguir
    RECHAZADO   // comprobante no válido
}
