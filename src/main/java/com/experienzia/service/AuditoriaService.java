package com.experienzia.service;

import com.experienzia.dto.AuditoriaDTO;

import java.util.List;

/**
 * Interfaz del servicio de auditoría.
 * Sirve para guardar un "historial" de lo que hacen los usuarios en el sistema
 * (quién hizo qué, sobre qué entidad y cuándo).
 */
/**
 * Interfaz del servicio AuditoriaService.
 * Define qué operaciones puede hacer el backend; la clase *Impl las programa.
 */
public interface AuditoriaService {

    /**
     * Versión corta de registrar: guarda la acción sin dirección IP.
     * Llama por dentro al método que sí recibe la IP.
     */
    default AuditoriaDTO registrar(Long usuarioId, String accion, String entidad, Long entidadId) {
        return registrar(usuarioId, accion, entidad, entidadId, null);
    }

    /**
     * Crea un registro de auditoría en la base de datos.
     * @param usuarioId quién hizo la acción (puede ser null si es anónimo)
     * @param accion texto que describe la acción (ej: "PAGO_APROBADO")
     * @param entidad nombre de la tabla o tipo (ej: "Pago", "Evento")
     * @param entidadId id del registro afectado
     * @param direccionIp IP del cliente, para trazabilidad
     */
    AuditoriaDTO registrar(Long usuarioId, String accion, String entidad, Long entidadId, String direccionIp);

    /** Devuelve todos los registros de auditoría, del más reciente al más antiguo. */
    List<AuditoriaDTO> listarTodo();

    /** Filtra el historial por el id de un usuario concreto. */
    List<AuditoriaDTO> listarPorUsuario(Long usuarioId);

    /** Filtra el historial por tipo de entidad (ej: solo "Inscripcion"). */
    List<AuditoriaDTO> listarPorEntidad(String entidad);
}
