package com.experienzia.service;

import com.experienzia.dto.NotificacionDTO;
import com.experienzia.entity.TipoNotificacion;

import java.util.List;

/**
 * Interfaz del servicio de notificaciones.
 * Permite avisar a los usuarios dentro de la app (mensajes en el panel de notificaciones).
 */
/**
 * Interfaz del servicio NotificacionService.
 * Define qué operaciones puede hacer el backend; la clase *Impl las programa.
 */
public interface NotificacionService {

    /** Crea una notificación nueva para un usuario (mensaje + tipo INFO, ALERTA, etc.). */
    NotificacionDTO crear(Long usuarioId, String mensaje, TipoNotificacion tipo);

    /** Devuelve las notificaciones de un usuario, ordenadas por fecha. */
    List<NotificacionDTO> listarPorUsuario(Long usuarioId);

    /** Marca una notificación como leída cuando el usuario la abre. */
    NotificacionDTO marcarLeida(Long id);
}
