package com.experienzia.service;

import com.experienzia.dto.EventoStaffDTO;
import com.experienzia.dto.StaffAsignadoDTO;
import com.experienzia.entity.Evento;
import com.experienzia.entity.FuncionStaff;

import java.util.List;

// Staff de logística en eventos: asignar, validar permisos en puerta y listar equipos
public interface StaffEventoService {

    // Organizador mete staff al evento con una función
    void asignarStaff(Long eventoId, Long organizadorId, Long staffUsuarioId, FuncionStaff funcion);

    // Cambio función de alguien ya asignado
    StaffAsignadoDTO cambiarFuncionStaff(Long eventoId, Long organizadorId, Long staffUsuarioId, FuncionStaff funcion);

    // Saco staff del evento
    void desasignarStaff(Long eventoId, Long organizadorId, Long staffUsuarioId);

    // Solo ids del staff del evento
    List<Long> listarStaffIdsPorEvento(Long eventoId);

    // Lista con datos para el front (nombre, función...)
    List<StaffAsignadoDTO> listarStaffPorEvento(Long eventoId);

    // Eventos donde trabajo como staff
    List<EventoStaffDTO> listarEventosDelStaff(Long staffUsuarioId);

    // Antes de check-in/out: si no está asignado freno y mando error
    void validarStaffAsignado(Long staffUsuarioId, Evento evento);

    // ¿Está asignado? (ej. no dejar que el staff se auto-inscriba como asistente)
    boolean existeAsignacion(Long staffUsuarioId, Long eventoId);
}
