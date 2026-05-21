package com.experienzia.spec;

import com.experienzia.entity.Estado;
import com.experienzia.entity.Rol;
import com.experienzia.entity.Usuario;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinámicos para buscar {@link Usuario} en listados de administración.
 * Igual que {@link EventoSpecification}: piezas de consulta que JPA une con AND.
 */
public class UsuarioSpecification {

    /** Parámetros de búsqueda enviados desde la UI o el controlador. */
    @Data
    public static class UsuarioSearchCriteria {
        private String nombre;
        private String email;
        private String rol;
        private String estado;
        private Long organizadorId;
    }

    /** Nombre contiene el texto (búsqueda parcial, case insensitive). */
    public static Specification<Usuario> hasNombre(String nombre) {
        return (root, query, cb) -> nombre == null || nombre.isEmpty()
                ? null
                : cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
    }

    /** Email contiene el texto buscado. */
    public static Specification<Usuario> hasEmail(String email) {
        return (root, query, cb) -> email == null || email.isEmpty()
                ? null
                : cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    /** Rol exacto: ASISTENTE, ORGANIZADOR, STAFF o ADMIN. */
    public static Specification<Usuario> hasRol(String rol) {
        return (root, query, cb) -> rol == null || rol.isEmpty()
                ? null
                : cb.equal(root.get("rol"), Rol.valueOf(rol.toUpperCase()));
    }

    /** Estado de cuenta: ACTIVO, PENDIENTE, etc. */
    public static Specification<Usuario> hasEstado(String estado) {
        return (root, query, cb) -> estado == null || estado.isEmpty()
                ? null
                : cb.equal(root.get("estado"), Estado.valueOf(estado.toUpperCase()));
    }

    /** Staff que pertenece a un organizador (campo organizadorId del usuario). */
    public static Specification<Usuario> hasOrganizadorId(Long organizadorId) {
        return (root, query, cb) -> organizadorId == null
                ? null
                : cb.equal(root.get("organizadorId"), organizadorId);
    }
}
