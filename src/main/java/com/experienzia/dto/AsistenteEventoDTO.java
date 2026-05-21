package com.experienzia.dto;

import com.experienzia.entity.EstadoInscripcion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Objeto de transferencia (DTO) para asistente evento. No es una tabla de la BD, solo lleva datos entre capas.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class AsistenteEventoDTO {
    /** Dato del campo inscripcion id */
    private Long inscripcionId;
    /** Id del usuario relacionado */
    private Long usuarioId;
    /** Nombre completo de la persona */
    private String nombre;
    /** Correo electrónico del usuario */
    private String email;
    /** Número de teléfono de contacto */
    private String telefono;
    /** Campo `tipoDocumento` (tipo documento) */
    private String tipoDocumento;
    /** Dato del campo numero documento */
    private String numeroDocumento;
    /** Código QR de la inscripción (útil para búsqueda y verificación en staff). */
    private String codigoQR;
    /** Dato del campo estado inscripcion */
    private EstadoInscripcion estadoInscripcion;
    /** Dato del campo fecha inscripcion */
    private LocalDateTime fechaInscripcion;
    /** Campo `fechaCheckIn` (fecha check in) */
    private LocalDateTime fechaCheckIn;
    /** Dato del campo fecha check out */
    private LocalDateTime fechaCheckOut;
}
