package com.experienzia.entity;

/**
 * Enum: qué clase de cambio pidió el organizador en un {@link EventoNovedad}.
 */
public enum TipoNovedadEvento {
    /** Cambió nombre, descripción, ubicación, etc. */
    EDICION_METADATOS,
    /** Cambió si es público/privado o la categoría. */
    EDICION_TIPO_CATEGORIA,
    /** Quiere alargar la duración (puede implicar pago extra). */
    AUMENTO_HORAS,
    /** Quiere acortar horas (puede tener penalización). */
    DISMINUCION_HORAS,
    /** Pide cancelar el evento; el admin decide. */
    CANCELACION_SOLICITUD
}
