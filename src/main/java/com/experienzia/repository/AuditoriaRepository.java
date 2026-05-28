package com.experienzia.repository;

import com.experienzia.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

// Acá leo y cuento el log de acciones de la app en la BD (quién movió qué y cuándo)
@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    // Para el panel admin: traigo todo el historial de más nuevo a más viejo al front
    List<Auditoria> findAllByOrderByFechaDesc();

    // Si el admin filtra por usuario, solo ve lo que hizo esa persona
    List<Auditoria> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    // Filtro por tipo (Evento, Usuario, Pago...) cuando eligen entidad en el front
    List<Auditoria> findByEntidadOrderByFechaDesc(String entidad);

    // Historial de un registro puntual y solo ciertas acciones (aprobar, rechazar...)
    List<Auditoria> findByEntidadAndEntidadIdAndAccionInOrderByFechaDesc(
            String entidad, Long entidadId, List<String> acciones);

    // Cuento en la BD sin traer filas: sirve para badges o resúmenes sin saturar el front
    @Query("""
            SELECT COUNT(a) FROM Auditoria a
            WHERE a.entidad = :entidad
            AND a.entidadId IN :entidadIds
            AND a.accion IN :acciones
            """)
    long countByEntidadAndEntidadIdInAndAccionIn(
            @Param("entidad") String entidad,
            @Param("entidadIds") Collection<Long> entidadIds,
            @Param("acciones") Collection<String> acciones);

    // Igual pero solo lo que hizo un usuario sobre varios ids (reportes del organizador)
    @Query("""
            SELECT COUNT(a) FROM Auditoria a
            WHERE a.usuarioId = :usuarioId
            AND a.entidad = :entidad
            AND a.entidadId IN :entidadIds
            AND a.accion IN :acciones
            """)
    long countByUsuarioIdAndEntidadAndEntidadIdInAndAccionIn(
            @Param("usuarioId") Long usuarioId,
            @Param("entidad") String entidad,
            @Param("entidadIds") Collection<Long> entidadIds,
            @Param("acciones") Collection<String> acciones);
}
