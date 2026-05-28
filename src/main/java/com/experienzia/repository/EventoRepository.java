package com.experienzia.repository;

import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.Evento;
import com.experienzia.entity.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// Todo lo de eventos contra la BD: catálogo, panel admin, organizador y reportes
@Repository
public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    // Catálogo público/privado filtrado por tipo y estado (lo que ve el front en listados)
    List<Evento> findByTipoEventoAndEstado(TipoEvento tipoEvento, EstadoEvento estado);

    // Panel admin: todos los que están PENDIENTE, ACTIVO, etc.
    List<Evento> findByEstado(EstadoEvento estado);

    // Mis eventos del organizador en el front
    List<Evento> findByOrganizadorId(Long organizadorId);

    // Cuántos eventos hay en un estado (tarjetas del dashboard)
    long countByEstado(EstadoEvento estado);

    // Cuántos creó un organizador en total
    long countByOrganizadorId(Long organizadorId);

    // Cuántos tiene en un estado concreto (ej. activos vs pendientes)
    long countByOrganizadorIdAndEstado(Long organizadorId, EstadoEvento estado);

    // Sumo aforo actual solo de los ACTIVOS del organizador para no mentirle al front en estadísticas
    @Query("""
            SELECT COALESCE(SUM(e.aforoActual), 0) FROM Evento e
            WHERE e.organizadorId = :organizadorId AND e.estado = com.experienzia.entity.EstadoEvento.ACTIVO
            """)
    long sumAforoActualEventosActivosByOrganizadorId(@Param("organizadorId") Long organizadorId);

    // Gráfica admin: cuántos eventos por mes desde una fecha
    @Query("""
            SELECT YEAR(e.fecha), MONTH(e.fecha), COUNT(e)
            FROM Evento e
            WHERE e.fecha >= :desde
            GROUP BY YEAR(e.fecha), MONTH(e.fecha)
            """)
    List<Object[]> countEventosAgrupadosPorMesDesde(@Param("desde") LocalDateTime desde);

    // Igual pero solo los de un organizador (su dashboard)
    @Query("""
            SELECT YEAR(e.fecha), MONTH(e.fecha), COUNT(e)
            FROM Evento e
            WHERE e.organizadorId = :organizadorId AND e.fecha >= :desde
            GROUP BY YEAR(e.fecha), MONTH(e.fecha)
            """)
    List<Object[]> countEventosAgrupadosPorMesByOrganizadorId(
            @Param("organizadorId") Long organizadorId,
            @Param("desde") LocalDateTime desde);

    // Detalle con organizador cargado de una: evito que el front pida datos sueltos después
    @Query("SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.organizador WHERE e.id = :id")
    Optional<Evento> findByIdWithOrganizador(@Param("id") Long id);

    // Calendario de salón: qué eventos chocan en la misma ubicación (comparo sin importar mayúsculas)
    @Query("""
            SELECT e FROM Evento e
            WHERE e.estado IN :estados
            AND LOWER(TRIM(COALESCE(e.ubicacion, ''))) = LOWER(TRIM(:ubicacion))
            """)
    List<Evento> findByUbicacionNormalizadaYEstadoIn(
            @Param("ubicacion") String ubicacion,
            @Param("estados") Collection<EstadoEvento> estados);
}
