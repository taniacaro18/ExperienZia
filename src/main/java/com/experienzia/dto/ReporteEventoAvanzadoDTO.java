package com.experienzia.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

// Reporte post-evento con curva horaria y tabla de desempeño del staff — más pesado que ReporteEventoDTO.
@Data

public class ReporteEventoAvanzadoDTO {

    private Long eventoId;

    private String nombreEvento;

    private LocalDateTime fechaEvento;

    private int aforoMaximo;

    private long inscritos;

    private long asistieron;

    private long faltaron;

    private double porcentajeOcupacion;

    private double porcentajeAsistencia;

    // Check-in/out desglosados
    private long checkInsTotal;

    private long checkInsPorQR;

    private long checkInsManuales;

    private long checkOutsTotal;

    private List<CurvaIngresoPuntoDTO> curvaIngreso;
    
    private List<DesempenoStaffDTO> desempenoStaff;
}
