package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Recibo esto en el API cuando el organizador asigna staff a un evento (body del POST).
@Data
@NoArgsConstructor
@AllArgsConstructor

public class AsignarStaffDTO {

    private Long organizadorId;

    private Long staffUsuarioId;

    private String funcion;
}
