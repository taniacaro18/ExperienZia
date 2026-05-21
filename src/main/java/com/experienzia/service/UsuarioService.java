package com.experienzia.service;

import com.experienzia.dto.ActualizarPerfilDTO;
import com.experienzia.dto.CrearStaffDTO;
import com.experienzia.dto.LoginDTO;
import com.experienzia.dto.RecuperarPasswordDTO;
import com.experienzia.dto.RecuperarPasswordResponseDTO;
import com.experienzia.dto.UsuarioDTO;
import com.experienzia.spec.UsuarioSpecification.UsuarioSearchCriteria;

import java.util.List;

/**
 * Interfaz del servicio de usuarios.
 * Gestiona registro, login, perfiles, roles y el ciclo de vida de las cuentas.
 */
/**
 * Interfaz del servicio UsuarioService.
 * Define qué operaciones puede hacer el backend; la clase *Impl las programa.
 */
public interface UsuarioService {

    /**
     * Registro público (HU-001 / HU-002).
     * El campo tipo del DTO decide si es ASISTENTE u ORGANIZADOR.
     */
    UsuarioDTO registrar(UsuarioDTO dto);

    /** Inicio de sesión con email y contraseña (HU-003). */
    UsuarioDTO login(LoginDTO dto);

    /** Un organizador crea una cuenta de personal STAFF (HU-004). */
    UsuarioDTO crearStaff(CrearStaffDTO dto);

    /** El admin aprueba un organizador que estaba PENDIENTE (HU-002a). */
    UsuarioDTO aprobarOrganizador(Long id);

    /** El admin rechaza un organizador pendiente (HU-002b). */
    UsuarioDTO rechazarOrganizador(Long id);

    /** El admin desactiva una cuenta (pasa a INACTIVO). */
    UsuarioDTO desactivar(Long id);

    /** El admin reactiva una cuenta que estaba INACTIVA (HU-019). */
    UsuarioDTO reactivar(Long id);

    /** El organizador desactiva un STAFF que él mismo creó. */
    UsuarioDTO desactivarStaffPorOrganizador(Long organizadorId, Long staffId);

    /** El organizador reactiva un STAFF que él mismo creó. */
    UsuarioDTO reactivarStaffPorOrganizador(Long organizadorId, Long staffId);

    /** El admin cambia el rol de un usuario (HU-018). */
    UsuarioDTO cambiarRol(Long id, String nuevoRol);

    /** Ver los datos de un usuario por su id (HU-005). */
    UsuarioDTO obtenerPorId(Long id);

    /** Editar teléfono y/o contraseña del perfil propio (HU-005). */
    UsuarioDTO actualizarPerfil(Long id, ActualizarPerfilDTO dto);

    /**
     * Recuperar contraseña sin enviar correo: devuelve una contraseña temporal
     * si el email y documento coinciden (HU-006).
     */
    RecuperarPasswordResponseDTO recuperarPassword(RecuperarPasswordDTO dto);

    /**
     * Reenvía credenciales a un asistente cargado en masa:
     * resetea la contraseña al número de documento (o una temporal) y notifica.
     */
    RecuperarPasswordResponseDTO reenviarCredenciales(Long usuarioId);

    /** Listado de todos los usuarios (solo uso admin). */
    List<UsuarioDTO> listarTodos();

    /** Busca usuarios por nombre, email, rol, estado u organizadorId. */
    List<UsuarioDTO> buscarPorCriterios(UsuarioSearchCriteria criteria);
}
