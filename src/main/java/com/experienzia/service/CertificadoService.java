package com.experienzia.service;

import com.experienzia.dto.CertificadoDTO;

import java.util.List;

/**
 * Interfaz del servicio de certificados.
 * Se encarga de crear y consultar los certificados de asistencia a eventos.
 */
/**
 * Interfaz del servicio CertificadoService.
 * Define qué operaciones puede hacer el backend; la clase *Impl las programa.
 */
public interface CertificadoService {

    /** Genera un certificado para una inscripción que ya marcó asistencia (ASISTIO). */
    CertificadoDTO generar(Long inscripcionId);

    /**
     * Genera certificados para todos los asistentes confirmados de un evento de una vez.
     * Solo el organizador del evento debería poder usarlo (HU-024).
     */
    List<CertificadoDTO> generarMasivoPorEvento(Long eventoId, Long organizadorId);

    /** Lista todos los certificados que tiene un usuario. */
    List<CertificadoDTO> listarPorUsuario(Long usuarioId);

    /** Lista todos los certificados emitidos para un evento. */
    List<CertificadoDTO> listarPorEvento(Long eventoId);

    /** Comprueba si un código único de certificado es válido (para la página de validación). */
    CertificadoDTO validarPorCodigo(String codigoUnico);
}
