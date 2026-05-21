package com.experienzia.repository;

import com.experienzia.entity.StaffEventoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA de {@link StaffEventoAsignacion}: qué staff trabaja en qué evento y con qué función.
 */
@Repository
public interface StaffEventoAsignacionRepository extends JpaRepository<StaffEventoAsignacion, Long> {

    /** true si ese staff ya está asignado al evento (evitar duplicados). */
    boolean existsByStaffUsuarioIdAndEventoId(Long staffUsuarioId, Long eventoId);

    /** Obtiene la asignación concreta staff + evento. */
    Optional<StaffEventoAsignacion> findByStaffUsuarioIdAndEventoId(Long staffUsuarioId, Long eventoId);

    /** Equipo de staff de un evento. */
    List<StaffEventoAsignacion> findByEventoId(Long eventoId);

    /** Eventos en los que trabaja un miembro del staff. */
    List<StaffEventoAsignacion> findByStaffUsuarioId(Long staffUsuarioId);
}
