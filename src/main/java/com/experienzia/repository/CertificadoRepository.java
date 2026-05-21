package com.experienzia.repository;

import com.experienzia.entity.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA de {@link Certificado}: emitidos por usuario, evento o código de verificación.
 */
@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Long> {

    /** Todos los certificados de un asistente. */
    List<Certificado> findByUsuarioId(Long usuarioId);

    /** Certificados generados para un evento. */
    List<Certificado> findByEventoId(Long eventoId);

    /** Validar un certificado desde su código público. */
    Optional<Certificado> findByCodigoUnico(String codigoUnico);

    /** ¿Ya tiene certificado este usuario en este evento? */
    Optional<Certificado> findByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);
}
