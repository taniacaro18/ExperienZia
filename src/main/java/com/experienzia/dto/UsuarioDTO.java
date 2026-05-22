package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class UsuarioDTO {

    private Long id;

    private String nombre;

    private String email;

    private String password;

    private String telefono;

    private String tipoDocumento;
    
    private String numeroDocumento;
  
    private String rol;

    private String estado;

    private Long organizadorId;

    private String tipo;
}
