package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body del login — solo email y password, nada de roles (eso lo saco yo de BD).
@Data
@NoArgsConstructor
@AllArgsConstructor

public class LoginDTO {

    private String email;
    
    private String password;
}
