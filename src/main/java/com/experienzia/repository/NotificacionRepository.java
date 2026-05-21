package com.experienzia.repository;

import com.experienzia.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA de {@link Notificacion}: bandeja de avisos por usuario.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /** Notificaciones de un usuario, las más recientes primero. */
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
