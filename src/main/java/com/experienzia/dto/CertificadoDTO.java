package com.experienzia.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Mapeo del certificado para el front/PDF cuando el asistente descarga o valida el código.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CertificadoDTO {

    private Long id;

    private Long usuarioId;

    private Long eventoId;

    private LocalDateTime fechaGeneracion;

    private String codigoUnico;

    // Textos que salen impresos en el certificado
    private String nombreAsistente;

    private String numeroDocumento;

    private String nombreEvento;

    private LocalDateTime fechaEvento;

    private Integer duracionHoras;

    private String nombreOrganizador;
    
    private String ciudadExpedicion;
}
