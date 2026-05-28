package com.experienzia.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Carga masiva: recibo varias filas y creo usuarios + inscripciones si no existían.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CargaAsistentesManualDTO {

    private Long organizadorId;
    
    private List<FilaAsistenteCargaDTO> filas;
}
