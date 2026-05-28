package com.experienzia.service;

import com.experienzia.dto.CertificadoDTO;

import java.util.List;

// Certificados de asistencia: generar, listar y validar el código que pega el front
public interface CertificadoService {

    // Genero uno cuando la inscripción ya quedó ASISTIO en la BD
    CertificadoDTO generar(Long inscripcionId);

    // El organizador pide todos los del evento de una (masivo desde el front)
    List<CertificadoDTO> generarMasivoPorEvento(Long eventoId, Long organizadorId);

    // Mis certificados como asistente
    List<CertificadoDTO> listarPorUsuario(Long usuarioId);

    // Todos los emitidos para un evento (organizador)
    List<CertificadoDTO> listarPorEvento(Long eventoId);

    // Página pública: si el código existe en la BD lo muestro, si no mando error
    CertificadoDTO validarPorCodigo(String codigoUnico);
}
