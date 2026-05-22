package com.experienzia.dto;

import java.time.LocalDateTime;

import com.experienzia.entity.EstadoInscripcion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AsistenteEventoDTO {
    private Long inscripcionId;

    private Long usuarioId;

    private String nombre;
    
    private String email;

    private String telefono;

    private String tipoDocumento;

    private String numeroDocumento;

    private String codigoQR;

    private EstadoInscripcion estadoInscripcion;

    private LocalDateTime fechaInscripcion;

    private LocalDateTime fechaCheckIn;
    
    private LocalDateTime fechaCheckOut;
}
