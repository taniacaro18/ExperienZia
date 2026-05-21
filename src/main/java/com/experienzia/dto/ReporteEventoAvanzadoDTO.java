package com.experienzia.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reporte avanzado del evento usado por las pantallas de "Análisis" del diseño:
 * - curva de ingreso por hora
 * - desglose asistieron / faltaron
 * - desglose por método (QR vs manual)
 * - desempeño por staff
 */
@Data
/**
 * Objeto de transferencia (DTO) para reporte evento avanzado. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class ReporteEventoAvanzadoDTO {
    /** Id del evento al que pertenece */
    private Long eventoId;
    /** Dato del campo nombre evento */
    private String nombreEvento;
    /** Campo `fechaEvento` (fecha evento) */
    private LocalDateTime fechaEvento;
    /** Dato del campo aforo maximo */
    private int aforoMaximo;
    /** Dato del campo inscritos */
    private long inscritos;
    /** Campo `asistieron` (asistieron) */
    private long asistieron;
    /** Dato del campo faltaron */
    private long faltaron;
    /** Dato del campo porcentaje ocupacion */
    private double porcentajeOcupacion;
    /** Campo `porcentajeAsistencia` (porcentaje asistencia) */
    private double porcentajeAsistencia;
    /** Dato del campo check ins total */
    private long checkInsTotal;
    /** Dato del campo check ins por q r */
    private long checkInsPorQR;
    /** Campo `checkInsManuales` (check ins manuales) */
    private long checkInsManuales;
    /** Dato del campo check outs total */
    private long checkOutsTotal;
    /** Dato del campo curva ingreso */
    private List<CurvaIngresoPuntoDTO> curvaIngreso;
    /** Campo `desempenoStaff` (desempeno staff) */
    private List<DesempenoStaffDTO> desempenoStaff;
}
