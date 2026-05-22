package com.experienzia.dto;

import java.time.LocalDateTime;

import com.experienzia.entity.EstadoPago;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PagoDTO {

    private Long id;

    private Long eventoId;

    private Long organizadorId;

    private String comprobanteUrl;

    private Double monto;

    private Double saldoAprobadoPrevio;

    private EstadoPago estado;

    private LocalDateTime fecha;

    private String motivoRechazo;
    
    
    private LocalDateTime fechaResolucion;

    private String nombreEvento;

    private LocalDateTime fechaEvento;

    private String nombreOrganizador;
    
    private String emailOrganizador;
}
