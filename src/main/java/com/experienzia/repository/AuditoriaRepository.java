package com.experienzia.repository;

import com.experienzia.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA de {@link Auditoria}: consultar el log de acciones del sistema.
 */
@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    /** Todo el historial, del más reciente al más antiguo. */
    List<Auditoria> findAllByOrderByFechaDesc();

    /** Acciones hechas por un usuario concreto. */
    List<Auditoria> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    /** Filtra por tipo de entidad (Evento, Usuario, etc.). */
    List<Auditoria> findByEntidadOrderByFechaDesc(String entidad);

    /** Historial de un registro concreto y solo ciertas acciones (ej. aprobar/rechazar). */
    List<Auditoria> findByEntidadAndEntidadIdAndAccionInOrderByFechaDesc(
            String entidad, Long entidadId, List<String> acciones);
}
