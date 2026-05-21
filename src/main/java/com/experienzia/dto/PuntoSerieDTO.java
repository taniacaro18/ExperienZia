package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sirve para los gráficos de los dashboards (líneas o barras).
 * Cada objeto es un punto: un periodo de tiempo y cuánto pasó en ese mes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para punto serie. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class PuntoSerieDTO {
    /** Mes o etiqueta del eje X, por ejemplo "2026-01" o "Ene". */
    private String periodo;
    /** Número que se pinta en el gráfico (eventos, usuarios, inscripciones, etc.). */
    private long valor;
}
