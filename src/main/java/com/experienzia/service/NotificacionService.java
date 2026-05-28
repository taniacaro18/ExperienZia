package com.experienzia.service;

import com.experienzia.dto.NotificacionDTO;
import com.experienzia.entity.TipoNotificacion;

import java.util.List;

// Avisos dentro de la app: creo en BD y el front los muestra en la campanita
public interface NotificacionService {

    // Creo notificación para un usuario (mensaje + tipo INFO, ALERTA...)
    NotificacionDTO crear(Long usuarioId, String mensaje, TipoNotificacion tipo);

    // Bandeja del usuario, más recientes primero
    List<NotificacionDTO> listarPorUsuario(Long usuarioId);

    // Cuando la abre en el front la marco leída en la BD
    NotificacionDTO marcarLeida(Long id);

    // Aviso a todos los admins activos (evento nuevo, pago subido, revisión pendiente...)
    void notificarAdministradores(String mensaje, TipoNotificacion tipo);
}
