package com.experienzia.impl;

import com.experienzia.dto.EventoStaffDTO;
import com.experienzia.dto.StaffAsignadoDTO;
import com.experienzia.entity.Estado;
import com.experienzia.entity.Evento;
import com.experienzia.entity.FuncionStaff;
import com.experienzia.entity.Rol;
import com.experienzia.entity.StaffEventoAsignacion;
import com.experienzia.entity.TipoNotificacion;
import com.experienzia.entity.Usuario;
import com.experienzia.exceptions.CustomException;
import com.experienzia.repository.EventoRepository;
import com.experienzia.repository.StaffEventoAsignacionRepository;
import com.experienzia.repository.UsuarioRepository;
import com.experienzia.service.NotificacionService;
import com.experienzia.service.StaffEventoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
// Staff por evento: asignar, cambiar función (logística, general…) y validar que puedan hacer check-in.
public class StaffEventoServiceImpl implements StaffEventoService {

    private final StaffEventoAsignacionRepository staffEventoRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    public StaffEventoServiceImpl(
            StaffEventoAsignacionRepository staffEventoRepository,
            EventoRepository eventoRepository,
            UsuarioRepository usuarioRepository,
            NotificacionService notificacionService) {
        this.staffEventoRepository = staffEventoRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
    }

    @Override
    // Si ya estaba asignado solo actualizo función; si no, creo fila y notifico al staff.
    public void asignarStaff(Long eventoId, Long organizadorId, Long staffUsuarioId, FuncionStaff funcion) {
        Evento evento = validarOrganizadorDeEvento(eventoId, organizadorId);
        validarStaffPropio(organizadorId, staffUsuarioId);

        // Si ya estaba asignado solo actualizo función; si no, creo fila y notifico al staff.
        FuncionStaff funcionFinal = funcion == null ? FuncionStaff.GENERAL : funcion;
        StaffEventoAsignacion existente = staffEventoRepository
                .findByStaffUsuarioIdAndEventoId(staffUsuarioId, eventoId)
                .orElse(null);
        if (existente != null) {
            if (existente.getFuncion() != funcionFinal) {
                existente.setFuncion(funcionFinal);
                staffEventoRepository.save(existente);
                notificacionService.crear(staffUsuarioId,
                        "Tu función en el evento \"" + evento.getNombre() + "\" cambió a: " + funcionFinal + ".",
                        TipoNotificacion.INFO);
            }
            return;
        }
        staffEventoRepository.save(new StaffEventoAsignacion(staffUsuarioId, eventoId, funcionFinal));
        notificacionService.crear(staffUsuarioId,
                "Se te asignó al evento \"" + evento.getNombre() + "\" con la función: " + funcionFinal + ".",
                TipoNotificacion.INFO);
    }

    @Override
    public StaffAsignadoDTO cambiarFuncionStaff(Long eventoId, Long organizadorId, Long staffUsuarioId, FuncionStaff funcion) {
        Evento evento = validarOrganizadorDeEvento(eventoId, organizadorId);
        StaffEventoAsignacion asignacion = staffEventoRepository
                .findByStaffUsuarioIdAndEventoId(staffUsuarioId, eventoId)
                .orElseThrow(() -> new CustomException("El staff no está asignado a este evento.", HttpStatus.NOT_FOUND));
        FuncionStaff nueva = funcion == null ? FuncionStaff.GENERAL : funcion;
        asignacion.setFuncion(nueva);
        StaffEventoAsignacion guardada = staffEventoRepository.save(asignacion);
        notificacionService.crear(staffUsuarioId,
                "Tu función en el evento \"" + evento.getNombre() + "\" cambió a: " + nueva + ".",
                TipoNotificacion.INFO);
        Usuario staff = usuarioRepository.findById(staffUsuarioId).orElse(null);
        return toStaffAsignadoDto(guardada, staff);
    }

    @Override
    public void desasignarStaff(Long eventoId, Long organizadorId, Long staffUsuarioId) {
        Evento evento = validarOrganizadorDeEvento(eventoId, organizadorId);
        StaffEventoAsignacion asignacion = staffEventoRepository
                .findByStaffUsuarioIdAndEventoId(staffUsuarioId, eventoId)
                .orElseThrow(() -> new CustomException("El staff no está asignado a este evento.", HttpStatus.NOT_FOUND));
        staffEventoRepository.delete(asignacion);
        notificacionService.crear(staffUsuarioId,
                "Fuiste removido del evento \"" + evento.getNombre() + "\".",
                TipoNotificacion.ALERTA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarStaffIdsPorEvento(Long eventoId) {
        return staffEventoRepository.findByEventoId(eventoId).stream()
                .map(StaffEventoAsignacion::getStaffUsuarioId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffAsignadoDTO> listarStaffPorEvento(Long eventoId) {
        return staffEventoRepository.findByEventoId(eventoId).stream()
                .map(a -> {
                    Usuario u = usuarioRepository.findById(a.getStaffUsuarioId()).orElse(null);
                    return toStaffAsignadoDto(a, u);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoStaffDTO> listarEventosDelStaff(Long staffUsuarioId) {
        return staffEventoRepository.findByStaffUsuarioId(staffUsuarioId).stream()
                .map(a -> {
                    Evento ev = eventoRepository.findById(a.getEventoId()).orElse(null);
                    return toEventoStaffDto(a, ev);
                })
                .filter(d -> d != null)
                .toList();
    }

    // Lo llamo desde check-in/out: tiene que ser STAFF de ese organizador y estar en la tabla de asignación.
    @Override
    // Lo uso en check-in: el staff solo opera eventos donde está asignado y del mismo organizador.
    public void validarStaffAsignado(Long staffUsuarioId, Evento evento) {
        Usuario staff = usuarioRepository.findById(staffUsuarioId)
                .orElseThrow(() -> new CustomException("Usuario no encontrado: " + staffUsuarioId, HttpStatus.NOT_FOUND));
        if (staff.getRol() != Rol.STAFF || staff.getOrganizadorId() == null) {
            throw new CustomException("La operación solo la puede ejecutar usuario STAFF.", HttpStatus.FORBIDDEN);
        }
        if (!staff.getOrganizadorId().equals(evento.getOrganizadorId())) {
            throw new CustomException("El staff no pertenece al organizador del evento.", HttpStatus.FORBIDDEN);
        }
        if (!staffEventoRepository.existsByStaffUsuarioIdAndEventoId(staffUsuarioId, evento.getId())) {
            throw new CustomException("El staff no está asignado a este evento.", HttpStatus.FORBIDDEN);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeAsignacion(Long staffUsuarioId, Long eventoId) {
        return staffEventoRepository.existsByStaffUsuarioIdAndEventoId(staffUsuarioId, eventoId);
    }

    private EventoStaffDTO toEventoStaffDto(StaffEventoAsignacion a, Evento ev) {
        if (ev == null) {
            return null;
        }
        EventoStaffDTO dto = new EventoStaffDTO();
        dto.setAsignacionId(a.getId());
        dto.setEventoId(ev.getId());
        dto.setNombreEvento(ev.getNombre());
        dto.setDescripcion(ev.getDescripcion());
        dto.setFechaEvento(ev.getFecha());
        dto.setUbicacion(ev.getUbicacion());
        dto.setTipoEvento(ev.getTipoEvento() != null ? ev.getTipoEvento().name() : null);
        dto.setEstadoEvento(ev.getEstado() != null ? ev.getEstado().name() : null);
        dto.setCategoria(ev.getCategoria());
        dto.setAforoMaximo(ev.getAforoMaximo());
        dto.setAforoActual(ev.getAforoActual());
        dto.setOrganizadorId(ev.getOrganizadorId());
        dto.setFuncion(a.getFuncion() == null ? FuncionStaff.GENERAL.name() : a.getFuncion().name());
        return dto;
    }

    // Freno si otro organizador intenta tocar el staff de un evento ajeno.
    private Evento validarOrganizadorDeEvento(Long eventoId, Long organizadorId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new CustomException("No se encontró el evento con ID: " + eventoId, HttpStatus.NOT_FOUND));
        if (organizadorId == null || !organizadorId.equals(evento.getOrganizadorId())) {
            throw new CustomException("Solo el organizador del evento puede gestionar su staff.", HttpStatus.FORBIDDEN);
        }
        return evento;
    }

    private void validarStaffPropio(Long organizadorId, Long staffUsuarioId) {
        Usuario staff = usuarioRepository.findById(staffUsuarioId)
                .orElseThrow(() -> new CustomException("Usuario staff no encontrado: " + staffUsuarioId, HttpStatus.NOT_FOUND));
        if (staff.getRol() != Rol.STAFF) {
            throw new CustomException("El usuario debe tener rol STAFF.", HttpStatus.FORBIDDEN);
        }
        if (!organizadorId.equals(staff.getOrganizadorId())) {
            throw new CustomException("El STAFF debe haber sido creado por el mismo organizador.", HttpStatus.FORBIDDEN);
        }
        if (staff.getEstado() != Estado.ACTIVO) {
            throw new CustomException("El STAFF no está ACTIVO. Estado actual: " + staff.getEstado() + ".", HttpStatus.FORBIDDEN);
        }
    }

    private StaffAsignadoDTO toStaffAsignadoDto(StaffEventoAsignacion a, Usuario u) {
        StaffAsignadoDTO dto = new StaffAsignadoDTO();
        dto.setAsignacionId(a.getId());
        dto.setStaffUsuarioId(a.getStaffUsuarioId());
        dto.setFuncion(a.getFuncion() == null ? FuncionStaff.GENERAL.name() : a.getFuncion().name());
        if (u != null) {
            dto.setNombre(u.getNombre());
            dto.setEmail(u.getEmail());
            dto.setTelefono(u.getTelefono());
            dto.setEstadoUsuario(u.getEstado() != null ? u.getEstado().name() : null);
        }
        return dto;
    }
}
