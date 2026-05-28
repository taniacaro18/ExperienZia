package com.experienzia.service;

import com.experienzia.dto.ActualizarPerfilDTO;
import com.experienzia.dto.CrearStaffDTO;
import com.experienzia.dto.LoginDTO;
import com.experienzia.dto.RecuperarPasswordDTO;
import com.experienzia.dto.RecuperarPasswordResponseDTO;
import com.experienzia.dto.UsuarioDTO;
import com.experienzia.spec.UsuarioSpecification.UsuarioSearchCriteria;

import java.util.List;

// Usuarios: registro, login, perfiles, roles y cuentas que admin/organizador gestionan
public interface UsuarioService {

    // Registro público: el tipo del DTO dice si es ASISTENTE u ORGANIZADOR
    UsuarioDTO registrar(UsuarioDTO dto);

    // Login con email y clave; si falla mando error al front
    UsuarioDTO login(LoginDTO dto);

    // Organizador crea cuenta de staff suyo
    UsuarioDTO crearStaff(CrearStaffDTO dto);

    // Admin aprueba organizador que estaba PENDIENTE
    UsuarioDTO aprobarOrganizador(Long id);

    // Admin rechaza organizador pendiente
    UsuarioDTO rechazarOrganizador(Long id);

    // Admin pasa cuenta a INACTIVO
    UsuarioDTO desactivar(Long id);

    // Admin reactiva cuenta INACTIVA
    UsuarioDTO reactivar(Long id);

    // Organizador desactiva un staff que él creó
    UsuarioDTO desactivarStaffPorOrganizador(Long organizadorId, Long staffId);

    // Organizador reactiva su staff
    UsuarioDTO reactivarStaffPorOrganizador(Long organizadorId, Long staffId);

    // Admin cambia rol de alguien
    UsuarioDTO cambiarRol(Long id, String nuevoRol);

    // Ver perfil por id
    UsuarioDTO obtenerPorId(Long id);

    // Usuario edita su teléfono y/o contraseña
    UsuarioDTO actualizarPerfil(Long id, ActualizarPerfilDTO dto);

    // Recuperar clave: si email y documento coinciden en la BD devuelvo temporal al front
    RecuperarPasswordResponseDTO recuperarPassword(RecuperarPasswordDTO dto);

    // Reenvío credenciales a asistente cargado en masa (reseteo clave y notifico)
    RecuperarPasswordResponseDTO reenviarCredenciales(Long usuarioId);

    // Listado total (solo admin)
    List<UsuarioDTO> listarTodos();

    // Búsqueda con filtros que manda el front
    List<UsuarioDTO> buscarPorCriterios(UsuarioSearchCriteria criteria);
}
