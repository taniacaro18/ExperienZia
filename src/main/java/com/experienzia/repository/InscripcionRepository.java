package com.experienzia.repository;

import com.experienzia.entity.EstadoInscripcion;
import com.experienzia.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Enlaces usuario–evento en la BD: inscripción, QR, check-in y reportes de asistencia
@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    // ¿Ya está apuntado a este evento? Si no hay fila el front deja inscribir
    Optional<Inscripcion> findByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);

    // Staff escanea el QR en puerta: busco por código en la BD
    Optional<Inscripcion> findByCodigoQR(String codigoQR);

    // Lista de asistentes de un evento (panel organizador/staff)
    List<Inscripcion> findByEventoId(Long eventoId);

    // Historial de eventos a los que se inscribió un usuario (su perfil)
    List<Inscripcion> findByUsuarioId(Long usuarioId);

    // Cuento inscripciones en un estado para aforo o reportes sin traer todo al front
    long countByEventoIdAndEstado(Long eventoId, EstadoInscripcion estado);

    // Cuántos siguen dentro (check-in hecho y sin check-out) para aforo en vivo
    long countByEventoIdAndEstadoAndFechaCheckOutIsNull(Long eventoId, EstadoInscripcion estado);

    // Ranking de eventos más populares (eventoId + total) para el admin
    @Query("SELECT i.eventoId, COUNT(i) AS total FROM Inscripcion i GROUP BY i.eventoId ORDER BY total DESC")
    List<Object[]> findEventosPopulares();

    // Solo ids de usuarios inscritos: mando notificaciones masivas sin cargar objetos enteros
    @Query("SELECT i.usuarioId FROM Inscripcion i WHERE i.eventoId = :eventoId")
    List<Long> findUsuarioIdsByEventoId(@Param("eventoId") Long eventoId);

    // Inscripciones activas (no canceladas) de todos los eventos de un organizador
    @Query("""
            SELECT COUNT(i) FROM Inscripcion i
            JOIN Evento e ON i.eventoId = e.id
            WHERE e.organizadorId = :organizadorId
            AND i.estado <> com.experienzia.entity.EstadoInscripcion.CANCELADO
            """)
    long countInscripcionesActivasByOrganizadorId(@Param("organizadorId") Long organizadorId);

    // Asistencias con check-in reciente del organizador (métricas del dashboard)
    @Query("""
            SELECT COUNT(i) FROM Inscripcion i
            JOIN Evento e ON i.eventoId = e.id
            WHERE e.organizadorId = :organizadorId
            AND i.fechaCheckIn IS NOT NULL
            AND i.fechaCheckIn > :desde
            """)
    long countAsistenciasConCheckInDesdeByOrganizadorId(
            @Param("organizadorId") Long organizadorId,
            @Param("desde") LocalDateTime desde);

    // Inscripciones activas por mes del organizador (gráfica en el front)
    @Query("""
            SELECT YEAR(i.fechaInscripcion), MONTH(i.fechaInscripcion), COUNT(i)
            FROM Inscripcion i
            JOIN Evento e ON i.eventoId = e.id
            WHERE e.organizadorId = :organizadorId
            AND i.estado <> com.experienzia.entity.EstadoInscripcion.CANCELADO
            AND i.fechaInscripcion >= :desde
            GROUP BY YEAR(i.fechaInscripcion), MONTH(i.fechaInscripcion)
            """)
    List<Object[]> countInscripcionesActivasAgrupadasPorMesByOrganizadorId(
            @Param("organizadorId") Long organizadorId,
            @Param("desde") LocalDateTime desde);
}
