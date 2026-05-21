package com.experienzia.repository;

import com.experienzia.entity.EstadoInscripcion;
import com.experienzia.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA de {@link Inscripcion}: enlaces usuario–evento, QR y estadísticas de asistencia.
 */
@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    /** Una inscripción concreta (¿ya está apuntado a este evento?). */
    Optional<Inscripcion> findByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);

    /** Busca por código QR escaneado en check-in. */
    Optional<Inscripcion> findByCodigoQR(String codigoQR);

    /** Lista de inscritos a un evento (lista de asistentes). */
    List<Inscripcion> findByEventoId(Long eventoId);

    /** Historial de eventos a los que se inscribió un usuario. */
    List<Inscripcion> findByUsuarioId(Long usuarioId);

    /** Cuenta inscripciones en un estado (para aforo o reportes). */
    long countByEventoIdAndEstado(Long eventoId, EstadoInscripcion estado);

    /** Cuenta asistentes dentro del evento que aún no hicieron check-out. */
    long countByEventoIdAndEstadoAndFechaCheckOutIsNull(Long eventoId, EstadoInscripcion estado);

    /** Ranking: eventoId y cuántas inscripciones tiene, ordenado de más a menos popular. */
    @Query("SELECT i.eventoId, COUNT(i) AS total FROM Inscripcion i GROUP BY i.eventoId ORDER BY total DESC")
    List<Object[]> findEventosPopulares();

    /** Solo los IDs de usuarios inscritos a un evento (para notificaciones masivas). */
    @Query("SELECT i.usuarioId FROM Inscripcion i WHERE i.eventoId = :eventoId")
    List<Long> findUsuarioIdsByEventoId(@Param("eventoId") Long eventoId);
}
