package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Usuario “seguro” para JSON — no mando la entidad JPA cruda al Angular.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class UsuarioDTO {

    // Identificación básica
    private Long id;

    private String nombre;

    private String email;

    private String password;

    // Contacto y documento (Colombia: CC, CE, etc.)
    private String telefono;

    private String tipoDocumento;
    
    private String numeroDocumento;
  
    // Rol y estado como texto para que Angular no dependa del enum del back
    private String rol;

    private String estado;

    // Si es STAFF, acá va el organizador que lo creó
    private Long organizadorId;

    private String tipo;
}
