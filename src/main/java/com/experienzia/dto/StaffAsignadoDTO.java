package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cómo listo al staff de un evento en el panel del organizador (viene de staff_evento_asignaciones).
@Data
@NoArgsConstructor
@AllArgsConstructor

public class StaffAsignadoDTO {

    private Long asignacionId;

    private Long staffUsuarioId;

    private String nombre;

    private String email;

    private String telefono;

    private String estadoUsuario;
    
    private String funcion;
}
