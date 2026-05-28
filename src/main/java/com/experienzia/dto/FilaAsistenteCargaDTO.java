package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Una fila dentro de CargaAsistentesManualDTO — datos mínimos para crear o inscribir al asistente.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class FilaAsistenteCargaDTO {

    private String nombre;

    private String email;

    private String telefono;

    private String tipoDocumento;
    
    private String numeroDocumento;
}
