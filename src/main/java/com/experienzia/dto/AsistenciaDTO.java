package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Contador simple de asistencias por evento — lo uso en reportes ligeros.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AsistenciaDTO {

    private Long eventoId;

    private Long totalAsistieron;
}
