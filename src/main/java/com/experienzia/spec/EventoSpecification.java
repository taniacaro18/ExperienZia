package com.experienzia.spec;

import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.Evento;
import com.experienzia.entity.TipoEvento;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Clase de ayuda para filtrar {@link Evento} en búsquedas dinámicas.
 * Cada método devuelve un {@link Specification}: Spring Data JPA los combina
 * (como un WHERE opcional por nombre, categoría, fechas, etc.).
 */
public class EventoSpecification {

    /** Criterios que llegan del formulario o API de búsqueda de eventos. */
    @Data
    public static class EventoSearchCriteria {
        private String nombre;
        private String categoria;
        private String tipoEvento;
        private String estado;
        private LocalDateTime fechaDesde;
        private LocalDateTime fechaHasta;
        private Long organizadorId;
    }

    /** Filtra por nombre parcial (contiene el texto, sin importar mayúsculas). */
    public static Specification<Evento> hasNombre(String nombre) {
        return (root, query, cb) -> nombre == null || nombre.isBlank()
                ? null
                : cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
    }

    /** Filtra por categoría parcial (ej. "deporte" encuentra "Deportes"). */
    public static Specification<Evento> hasCategoria(String categoria) {
        return (root, query, cb) -> categoria == null || categoria.isBlank()
                ? null
                : cb.like(cb.lower(root.get("categoria")), "%" + categoria.toLowerCase() + "%");
    }

    /** Filtra PUBLICO o PRIVADO; el texto se convierte a enum. */
    public static Specification<Evento> hasTipo(String tipo) {
        return (root, query, cb) -> tipo == null || tipo.isBlank()
                ? null
                : cb.equal(root.get("tipoEvento"), TipoEvento.valueOf(tipo.toUpperCase()));
    }

    /** Filtra por estado del evento (ACTIVO, PENDIENTE, etc.). */
    public static Specification<Evento> hasEstado(String estado) {
        return (root, query, cb) -> estado == null || estado.isBlank()
                ? null
                : cb.equal(root.get("estado"), EstadoEvento.valueOf(estado.toUpperCase()));
    }

    /** Eventos con fecha de inicio mayor o igual a esta fecha. */
    public static Specification<Evento> fechaDesde(LocalDateTime desde) {
        return (root, query, cb) -> desde == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("fecha"), desde);
    }

    /** Eventos con fecha de inicio menor o igual a esta fecha. */
    public static Specification<Evento> fechaHasta(LocalDateTime hasta) {
        return (root, query, cb) -> hasta == null
                ? null
                : cb.lessThanOrEqualTo(root.get("fecha"), hasta);
    }

    /** Solo eventos de un organizador (panel "mis eventos" o admin). */
    public static Specification<Evento> hasOrganizador(Long organizadorId) {
        return (root, query, cb) -> organizadorId == null
                ? null
                : cb.equal(root.get("organizadorId"), organizadorId);
    }
}
