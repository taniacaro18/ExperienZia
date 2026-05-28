package com.experienzia.service;

import com.experienzia.dto.AuditoriaDTO;

import java.util.List;

// Contrato del historial de acciones: guardo en BD quién hizo qué y el front lo consulta
public interface AuditoriaService {

    // Atajo sin IP: por dentro llamo al que sí guarda la IP en la BD
    default AuditoriaDTO registrar(Long usuarioId, String accion, String entidad, Long entidadId) {
        return registrar(usuarioId, accion, entidad, entidadId, null);
    }

    // Creo un registro en la BD (usuario, acción, entidad, id y opcional la IP del front)
    AuditoriaDTO registrar(Long usuarioId, String accion, String entidad, Long entidadId, String direccionIp);

    // Panel admin: todo el log del más reciente al más viejo
    List<AuditoriaDTO> listarTodo();

    // Solo lo que hizo un usuario (filtro del front)
    List<AuditoriaDTO> listarPorUsuario(Long usuarioId);

    // Solo un tipo de entidad (Evento, Pago, Inscripcion...)
    List<AuditoriaDTO> listarPorEntidad(String entidad);
}
