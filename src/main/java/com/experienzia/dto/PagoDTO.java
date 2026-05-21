package com.experienzia.dto;

import com.experienzia.entity.EstadoPago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Información de un pago de inscripción.
 * (DTO = solo transporta datos entre frontend, controlador y servicio)
 */
public class PagoDTO {
    /** Identificador único en la base de datos */
    private Long id;
    /** Id del evento al que pertenece */
    private Long eventoId;
    /** Campo `organizadorId` (organizador id) */
    private Long organizadorId;
    /** Dato del campo comprobante url */
    private String comprobanteUrl;
    /** Dato del campo monto */
    private Double monto;
    /** Si no es null, el comprobante pendiente cubre solo la diferencia sobre este monto ya aprobado. */
    private Double saldoAprobadoPrevio;
    /** Estado actual (ACTIVO, PENDIENTE, etc.) */
    private EstadoPago estado;
    /** Dato del campo fecha */
    private LocalDateTime fecha;
    /** Campo `motivoRechazo` (motivo rechazo) */
    private String motivoRechazo;
    /** Dato del campo aprobador id */
    private Long aprobadorId;
    /** Dato del campo fecha resolucion */
    private LocalDateTime fechaResolucion;

    /** Datos extra del evento (solo lectura) para mostrar en la UI sin hacer otra llamada. */
    private String nombreEvento;
    /** Dato del campo fecha evento */
    private LocalDateTime fechaEvento;
    /** Datos extra del organizador. */
    private String nombreOrganizador;
    /** Dato del campo email organizador */
    private String emailOrganizador;
}
