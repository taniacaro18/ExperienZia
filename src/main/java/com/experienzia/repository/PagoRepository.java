package com.experienzia.repository;

import com.experienzia.entity.EstadoPago;
import com.experienzia.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA de {@link Pago}: pagos de tarifa de plataforma por evento y organizador.
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /** El pago asociado a un evento (suele haber uno activo por flujo). */
    Optional<Pago> findByEventoId(Long eventoId);

    /** Cola de pagos por estado (ej. todos los PENDIENTE para el admin). */
    List<Pago> findByEstado(EstadoPago estado);

    /** Historial de pagos de un organizador. */
    List<Pago> findByOrganizadorId(Long organizadorId);
}
