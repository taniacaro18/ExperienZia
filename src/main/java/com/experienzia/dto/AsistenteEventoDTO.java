package com.experienzia.dto;

import java.time.LocalDateTime;

import com.experienzia.entity.EstadoInscripcion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Fila de asistente en reportes/listados — junta datos de inscripción + usuario sin ir al front a otra API.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AsistenteEventoDTO {
    private Long inscripcionId;

    private Long usuarioId;

    // Datos personales para la tabla
    private String nombre;
    
    private String email;

    private String telefono;

    private String tipoDocumento;

    private String numeroDocumento;

    // QR y estado de asistencia el día del evento
    private String codigoQR;

    private EstadoInscripcion estadoInscripcion;

    private LocalDateTime fechaInscripcion;

    private LocalDateTime fechaCheckIn;
    
    private LocalDateTime fechaCheckOut;
}
