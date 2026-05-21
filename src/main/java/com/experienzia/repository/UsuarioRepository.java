package com.experienzia.repository;

import com.experienzia.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Usuario}.
 * Hereda guardar/buscar/borrar por ID y además permite filtros dinámicos con {@link com.experienzia.spec.UsuarioSpecification}.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    /** Busca un usuario por correo (útil en login). */
    Optional<Usuario> findByEmail(String email);

    /** true si ya hay alguien con ese email (validar registro). */
    boolean existsByEmail(String email);

    /** true si el número de documento ya está registrado. */
    boolean existsByNumeroDocumento(String numeroDocumento);

    /** true si el teléfono ya está en uso. */
    boolean existsByTelefono(String telefono);
}
