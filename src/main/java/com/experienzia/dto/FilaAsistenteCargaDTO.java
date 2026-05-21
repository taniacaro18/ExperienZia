package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para fila asistente carga. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class FilaAsistenteCargaDTO {
    /** Nombre completo de la persona */
    private String nombre;
    /** Correo electrónico del usuario */
    private String email;
    /** Número de teléfono de contacto */
    private String telefono;
    /** Dato del campo tipo documento */
    private String tipoDocumento;
    /** Dato del campo numero documento */
    private String numeroDocumento;
}
