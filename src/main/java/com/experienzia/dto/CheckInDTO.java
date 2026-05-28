package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body del check-in/out: recibo staff, QR (o manual) y evento para actualizar la inscripción.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CheckInDTO {

    private Long staffUsuarioId;

    private String codigoQR;
    
    private Long eventoId;
}
