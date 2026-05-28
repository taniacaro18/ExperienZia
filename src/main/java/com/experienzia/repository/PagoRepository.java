package com.experienzia.repository;

import com.experienzia.entity.EstadoPago;
import com.experienzia.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Pagos de tarifa de plataforma por evento: comprobante, aprobación admin y activación
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    // Un evento suele tener un pago asociado: lo busco antes de dejar subir otro comprobante
    Optional<Pago> findByEventoId(Long eventoId);

    // Cola del admin: todos los PENDIENTE para aprobar o rechazar desde el front
    List<Pago> findByEstado(EstadoPago estado);

    // Historial de pagos de un organizador (su panel)
    List<Pago> findByOrganizadorId(Long organizadorId);

    List<Pago> findAllByOrderByFechaDesc();

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = :estado")
    double sumMontoByEstado(@Param("estado") EstadoPago estado);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = :estado AND p.saldoAprobadoPrevio IS NOT NULL")
    double sumMontoComplementosByEstado(@Param("estado") EstadoPago estado);

    long countByEstado(EstadoPago estado);
}
