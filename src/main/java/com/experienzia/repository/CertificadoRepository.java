package com.experienzia.repository;

import com.experienzia.entity.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Certificados emitidos en la BD: por usuario, evento o código de verificación pública
@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Long> {

    // Todos los certificados de un asistente (su lista en el front)
    List<Certificado> findByUsuarioId(Long usuarioId);

    // Certificados de un evento (organizador revisa quién ya tiene)
    List<Certificado> findByEventoId(Long eventoId);

    // Página pública de validación: busco por el código que pegan en el front
    Optional<Certificado> findByCodigoUnico(String codigoUnico);

    // Evito duplicar: ¿ya le generé certificado a este usuario en este evento?
    Optional<Certificado> findByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);
}
