package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body para crear un usuario STAFF desde el panel del organizador (lo persisto en usuarios).
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CrearStaffDTO {

    // Datos de la cuenta nueva
    private String nombre;

    private String email;

    private String password;

    private String telefono;

    private String tipoDocumento;

    private String numeroDocumento;

    // A quién pertenece este staff
    private Long organizadorId;
}
