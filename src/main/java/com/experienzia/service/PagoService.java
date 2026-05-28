package com.experienzia.service;

import com.experienzia.dto.PagoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

// Pagos de tarifa de plataforma: organizador sube comprobante, admin aprueba y activa evento
public interface PagoService {

    // Organizador sube comprobante; calculo monto desde horas del evento y guardo en la BD
    PagoDTO registrar(Long eventoId, Long organizadorId, MultipartFile archivo, String direccionIp);

    // Admin aprueba: puede activar evento e inscribir al organizador
    PagoDTO aprobar(Long pagoId, Long aprobadorId, String direccionIp);

    // Admin rechaza con motivo para el front del organizador
    PagoDTO rechazar(Long pagoId, String motivo, Long aprobadorId, String direccionIp);

    // Cola de pagos PENDIENTE para el panel admin
    List<PagoDTO> listarPendientes();

    // Todos los pagos (admin)
    List<PagoDTO> listarTodos();

    // Historial de un organizador
    List<PagoDTO> listarPorOrganizador(Long organizadorId);

    // ¿Ya hay pago de este evento? (puede no existir)
    Optional<PagoDTO> obtenerPorEvento(Long eventoId);
}
