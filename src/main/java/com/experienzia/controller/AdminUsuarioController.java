package com.experienzia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experienzia.dto.CambiarRolDTO;
import com.experienzia.dto.UsuarioDTO;
import com.experienzia.entity.TipoNotificacion;
import com.experienzia.exceptions.CustomException;
import com.experienzia.service.AuditoriaService;
import com.experienzia.service.NotificacionService;
import com.experienzia.service.UsuarioService;
import com.experienzia.util.ClientIpResolver;

import jakarta.servlet.http.HttpServletRequest;

// Panel admin: aprobar organizadores, desactivar cuentas, cambiar roles
@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;

    public AdminUsuarioController(UsuarioService usuarioService,
                                  NotificacionService notificacionService,
                                  AuditoriaService auditoriaService) {
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
    }

    // Organizador nuevo queda ACTIVO y le llega noti al front
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<UsuarioDTO> aprobarOrganizador(
            @PathVariable Long id,
            @RequestParam(required = false) Long adminId,
            HttpServletRequest request) {
        UsuarioDTO u = usuarioService.aprobarOrganizador(id);
        notificacionService.crear(u.getId(),
                "Tu cuenta de organizador fue aprobada. Ya puedes iniciar sesión.",
                TipoNotificacion.INFO);
        auditoriaService.registrar(adminId, "ORGANIZADOR_APROBADO", "Usuario", u.getId(),
                ClientIpResolver.resolve(request));
        return ResponseEntity.ok(u);
    }

    // Organizador que pidió cuenta y el admin dice que no
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<UsuarioDTO> rechazarOrganizador(@PathVariable Long id,
                                                          @RequestParam(required = false) Long adminId,
                                                          HttpServletRequest request) {
        UsuarioDTO u = usuarioService.rechazarOrganizador(id);
        notificacionService.crear(u.getId(),
                "Tu solicitud de organizador fue rechazada por el administrador.",
                TipoNotificacion.ALERTA);
        auditoriaService.registrar(adminId, "ORGANIZADOR_RECHAZADO", "Usuario", u.getId(),
                ClientIpResolver.resolve(request));
        return ResponseEntity.ok(u);
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioDTO> desactivar(@PathVariable Long id,
                                                 @RequestParam(required = false) Long adminId,
                                                 HttpServletRequest request) {
        UsuarioDTO u = usuarioService.desactivar(id);
        notificacionService.crear(u.getId(),
                "Tu cuenta fue desactivada por el administrador.",
                TipoNotificacion.ALERTA);
        auditoriaService.registrar(adminId, "USUARIO_DESACTIVADO", "Usuario", u.getId(),
                ClientIpResolver.resolve(request));
        return ResponseEntity.ok(u);
    }

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<UsuarioDTO> reactivar(@PathVariable Long id,
                                                @RequestParam(required = false) Long adminId,
                                                HttpServletRequest request) {
        UsuarioDTO u = usuarioService.reactivar(id);
        notificacionService.crear(u.getId(),
                "Tu cuenta fue reactivada por el administrador. Ya puedes iniciar sesión.",
                TipoNotificacion.INFO);
        auditoriaService.registrar(adminId, "USUARIO_REACTIVADO", "Usuario", u.getId(),
                ClientIpResolver.resolve(request));
        return ResponseEntity.ok(u);
    }

    // Cambio rol (STAFF, ORGANIZADOR...) — adminId en query es para auditoría nada más
    @PutMapping("/{id}/rol")
    public ResponseEntity<UsuarioDTO> cambiarRol(@PathVariable Long id,
                                                 @RequestBody CambiarRolDTO body,
                                                 @RequestParam(required = false) Long adminId,
                                                 HttpServletRequest request) {
        if (body == null || body.getRol() == null) {
            throw new CustomException("El rol es obligatorio.", HttpStatus.BAD_REQUEST);
        }
        UsuarioDTO u = usuarioService.cambiarRol(id, body.getRol());
        notificacionService.crear(u.getId(),
                "Tu rol fue actualizado por el administrador. Nuevo rol: " + u.getRol() + ".",
                TipoNotificacion.INFO);
        auditoriaService.registrar(adminId, "ROL_CAMBIADO_A_" + u.getRol(), "Usuario", u.getId(),
                ClientIpResolver.resolve(request));
        return ResponseEntity.ok(u);
    }
}
