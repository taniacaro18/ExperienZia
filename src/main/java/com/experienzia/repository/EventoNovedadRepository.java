package com.experienzia.repository;

import com.experienzia.entity.EstadoNovedadEvento;
import com.experienzia.entity.EventoNovedad;
import com.experienzia.entity.TipoNovedadEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA de {@link EventoNovedad}: historial y solicitudes pendientes de cambio en eventos.
 */
public interface EventoNovedadRepository extends JpaRepository<EventoNovedad, Long> {

    /** Todas las novedades de un evento, la más nueva primero. */
    List<EventoNovedad> findByEventoIdOrderByFechaSolicitudDesc(Long eventoId);

    /** La última novedad de un evento en un estado (ej. la PENDIENTE más reciente). */
    Optional<EventoNovedad> findFirstByEventoIdAndEstadoOrderByFechaSolicitudDesc(
            Long eventoId, EstadoNovedadEvento estado);

    /** Igual que arriba pero filtrando también por tipo (cancelación, edición, etc.). */
    Optional<EventoNovedad> findFirstByEventoIdAndEstadoAndTipoOrderByFechaSolicitudDesc(
            Long eventoId, EstadoNovedadEvento estado, TipoNovedadEvento tipo);
}
