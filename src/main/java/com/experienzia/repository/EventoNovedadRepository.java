package com.experienzia.repository;

import com.experienzia.entity.EstadoNovedadEvento;
import com.experienzia.entity.EventoNovedad;
import com.experienzia.entity.TipoNovedadEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Solicitudes de cambio/cancelación de eventos: historial y la PENDIENTE más reciente
public interface EventoNovedadRepository extends JpaRepository<EventoNovedad, Long> {

    // Historial de novedades de un evento, la más nueva primero (panel admin/organizador)
    List<EventoNovedad> findByEventoIdOrderByFechaSolicitudDesc(Long eventoId);

    // La última novedad en un estado (ej. la PENDIENTE que el admin debe revisar)
    Optional<EventoNovedad> findFirstByEventoIdAndEstadoOrderByFechaSolicitudDesc(
            Long eventoId, EstadoNovedadEvento estado);

    // Igual pero por tipo también (cancelación vs edición vs suplemento de pago)
    Optional<EventoNovedad> findFirstByEventoIdAndEstadoAndTipoOrderByFechaSolicitudDesc(
            Long eventoId, EstadoNovedadEvento estado, TipoNovedadEvento tipo);
}
