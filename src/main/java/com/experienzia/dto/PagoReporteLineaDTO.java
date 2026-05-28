package com.experienzia.dto;

import com.experienzia.entity.EstadoPago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Una fila del reporte de pagos para el admin (tabla + PDF)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoReporteLineaDTO {
    private Long pagoId;
    private Long eventoId;
    private String nombreEvento;
    private Long organizadorId;
    private String nombreOrganizador;
    private double monto;
    private EstadoPago estado;
    private LocalDateTime fecha;
    private boolean complementoHoras;
}
