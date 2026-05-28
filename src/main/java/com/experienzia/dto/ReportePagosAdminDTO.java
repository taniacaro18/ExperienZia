package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Resumen analítico de pagos para el panel admin
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportePagosAdminDTO {
    private double totalRecaudadoAprobado;
    private double totalComplementosHorasAprobados;
    private double totalPendienteValidacion;
    private long cantidadAprobadas;
    private long cantidadPendientes;
    private long cantidadRechazadas;
    private List<PagoReporteLineaDTO> transacciones;
}
