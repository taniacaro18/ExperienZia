package com.experienzia.impl;

import com.experienzia.dto.NotificacionDTO;
import com.experienzia.entity.Estado;
import com.experienzia.entity.Notificacion;
import com.experienzia.entity.Rol;
import com.experienzia.entity.TipoNotificacion;
import com.experienzia.entity.Usuario;
import com.experienzia.exceptions.CustomException;
import com.experienzia.repository.NotificacionRepository;
import com.experienzia.repository.UsuarioRepository;
import com.experienzia.service.NotificacionService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Campanitas del panel: staff asignado, pago rechazado, carga CSV, etc.
@Service
@Transactional
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public NotificacionServiceImpl(
            NotificacionRepository notificacionRepository,
            UsuarioRepository usuarioRepository,
            ModelMapper modelMapper) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    // Creo el mensaje y lo guardo; el front hace polling o lista por usuario.
    public NotificacionDTO crear(Long usuarioId, String mensaje, TipoNotificacion tipo) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioId(usuarioId);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo == null ? TipoNotificacion.INFO : tipo);
        notificacion.setLeida(false);
        notificacion.setFecha(LocalDateTime.now());
        return modelMapper.map(notificacionRepository.save(notificacion), NotificacionDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    // Las más nuevas primero, como una bandeja de entrada
    public List<NotificacionDTO> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(n -> modelMapper.map(n, NotificacionDTO.class))
                .toList();
    }

    @Override
    // Cuando el usuario abre la noti en el front, la marco leída
    public NotificacionDTO marcarLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Notificación no encontrada.", HttpStatus.NOT_FOUND));
        notificacion.setLeida(true);
        return modelMapper.map(notificacionRepository.save(notificacion), NotificacionDTO.class);
    }

    @Override
    // El bug del admin era que nadie le creaba notis: ahora duplico el aviso a cada ADMIN activo
    public void notificarAdministradores(String mensaje, TipoNotificacion tipo) {
        if (mensaje == null || mensaje.isBlank()) {
            return;
        }
        List<Usuario> admins = usuarioRepository.findByRolAndEstado(Rol.ADMIN, Estado.ACTIVO);
        for (Usuario admin : admins) {
            crear(admin.getId(), mensaje, tipo);
        }
    }
}
