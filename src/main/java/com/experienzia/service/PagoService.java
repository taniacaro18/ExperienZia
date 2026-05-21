package com.experienzia.service;

import com.experienzia.dto.PagoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz del servicio de pagos de eventos.
 * El organizador paga la tarifa de la plataforma para activar su evento;
 * el administrador aprueba o rechaza el comprobante.
 */
/**
 * Interfaz del servicio PagoService.
 * Define qué operaciones puede hacer el backend; la clase *Impl las programa.
 */
public interface PagoService {

    /**
     * El organizador sube el comprobante de pago de la tarifa del evento.
     * El monto se calcula solo desde el costo del evento (horas × precio por hora).
     */
    PagoDTO registrar(Long eventoId, Long organizadorId, MultipartFile archivo, String direccionIp);

    /** El admin aprueba el pago; eso puede activar el evento e inscribir al organizador. */
    PagoDTO aprobar(Long pagoId, Long aprobadorId, String direccionIp);

    /** El admin rechaza el pago y debe indicar el motivo. */
    PagoDTO rechazar(Long pagoId, String motivo, Long aprobadorId, String direccionIp);

    /** Lista pagos que están esperando revisión del admin. */
    List<PagoDTO> listarPendientes();

    /** Lista todos los pagos del sistema. */
    List<PagoDTO> listarTodos();

    /** Lista los pagos hechos por un organizador concreto. */
    List<PagoDTO> listarPorOrganizador(Long organizadorId);

    /** Busca si ya hay un pago asociado a un evento (puede no existir). */
    Optional<PagoDTO> obtenerPorEvento(Long eventoId);
}
