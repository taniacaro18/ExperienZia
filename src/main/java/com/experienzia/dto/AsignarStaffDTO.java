package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AsignarStaffDTO {

    private Long organizadorId;

    private Long staffUsuarioId;

    private String funcion;
}
