package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body al rechazar un pago: motivo para el organizador y id del admin que resolvió.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RechazarPagoDTO {

    private String motivo;
    
    private Long aprobadorId;
}
