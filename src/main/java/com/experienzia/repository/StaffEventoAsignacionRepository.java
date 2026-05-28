package com.experienzia.repository;

import com.experienzia.entity.StaffEventoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Quién del staff trabaja en qué evento y con qué función (check-in QR, manual, etc.)
@Repository
public interface StaffEventoAsignacionRepository extends JpaRepository<StaffEventoAsignacion, Long> {

    // Antes de asignar reviso en la BD: si ya está, freno y mando error al front
    boolean existsByStaffUsuarioIdAndEventoId(Long staffUsuarioId, Long eventoId);

    // La fila concreta staff + evento (cambiar función o validar permisos)
    Optional<StaffEventoAsignacion> findByStaffUsuarioIdAndEventoId(Long staffUsuarioId, Long eventoId);

    // Equipo completo de un evento (lista en panel organizador)
    List<StaffEventoAsignacion> findByEventoId(Long eventoId);

    // Eventos donde trabaja un staff (su panel en el front)
    List<StaffEventoAsignacion> findByStaffUsuarioId(Long staffUsuarioId);
}
