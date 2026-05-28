package com.experienzia.entity;

// Qué pidió cambiar el organizador en evento_novedades — el admin revisa según el tipo.
public enum TipoNovedadEvento {
    // Cambios de texto/lugar sin tocar horas de pago
    EDICION_METADATOS,
    EDICION_TIPO_CATEGORIA,
    // Cambios de duración (pueden generar suplemento o penalización)
    AUMENTO_HORAS,
    DISMINUCION_HORAS,
    // Quiere cancelar el evento
    CANCELACION_SOLICITUD
}
