package com.experienzia.repository;

import com.experienzia.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Bandeja de avisos por usuario en la BD (campanita del front)
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    // Notificaciones de un usuario, las más recientes primero para pintar la lista
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
