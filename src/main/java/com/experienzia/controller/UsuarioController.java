package com.experienzia.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experienzia.dto.ActualizarPerfilDTO;
import com.experienzia.dto.CrearStaffDTO;
import com.experienzia.dto.LoginDTO;
import com.experienzia.dto.LoginResponseDTO;
import com.experienzia.dto.RecuperarPasswordDTO;
import com.experienzia.dto.RecuperarPasswordResponseDTO;
import com.experienzia.dto.UsuarioDTO;
import com.experienzia.exceptions.CustomException;
import com.experienzia.security.JwtService;
import com.experienzia.service.AuditoriaService;
import com.experienzia.service.UsuarioService;
import com.experienzia.spec.UsuarioSpecification.UsuarioSearchCriteria;
import com.experienzia.util.ClientIpResolver;

import jakarta.servlet.http.HttpServletRequest;

// Yo expongo login, registro y CRUD de usuarios para el front
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;
    private final JwtService jwtService;

    public UsuarioController(UsuarioService usuarioService, AuditoriaService auditoriaService,
                             JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
        this.jwtService = jwtService;
    }

    // Registro público (asistente u organizador según lo que mande el front)
    // Registro público — asistente u organizador (organizador queda PENDIENTE hasta que admin apruebe)
    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registrar(@RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.registrar(dto));
    }

    // Acá valido credenciales y devuelvo el token que el front guarda
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO dto) {
        UsuarioDTO usuario = usuarioService.login(dto);
        String token = jwtService.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol());
        return ResponseEntity.ok(new LoginResponseDTO(token, usuario));
    }

    // Solo organizador crea staff ligado a su cuenta
    // El organizador crea cuentas staff ligadas a su id
    @PostMapping("/staff")
    public ResponseEntity<UsuarioDTO> crearStaff(@RequestBody CrearStaffDTO dto) {
        return ResponseEntity.ok(usuarioService.crearStaff(dto));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // Admin filtra usuarios por nombre, rol, estado...
    // Filtros del panel admin (nombre, rol, estado...)
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTO>> buscar(UsuarioSearchCriteria criteria) {
        return ResponseEntity.ok(usuarioService.buscarPorCriterios(criteria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarPerfil(@PathVariable Long id,
                                                       @RequestBody ActualizarPerfilDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(id, dto));
    }

    // Olvidé mi clave — mando correo o mensaje según lo que tengamos en el service
    // Olvidé mi clave: mando correo o mensaje según lo que tenga el service
    @PostMapping("/recuperar")
    public ResponseEntity<RecuperarPasswordResponseDTO> recuperar(@RequestBody RecuperarPasswordDTO dto) {
        return ResponseEntity.ok(usuarioService.recuperarPassword(dto));
    }

    // Reenvío credenciales; freneo si quien llama es ADMIN (regla de negocio rara pero así quedó)
    @PostMapping("/{id}/reenviar-credenciales")
    public ResponseEntity<RecuperarPasswordResponseDTO> reenviarCredenciales(
            @PathVariable Long id,
            @RequestParam(required = false) Long actorId,
            HttpServletRequest request) {
        if (autenticadoEsAdmin()) {
            throw new CustomException(
                    "Los administradores no pueden restablecer contraseñas de otros usuarios.",
                    HttpStatus.FORBIDDEN);
        }
        RecuperarPasswordResponseDTO r = usuarioService.reenviarCredenciales(id);
        auditoriaService.registrar(actorId, "CREDENCIALES_REENVIADAS", "Usuario", id,
                ClientIpResolver.resolve(request));
        return ResponseEntity.ok(r);
    }

    private static boolean autenticadoEsAdmin() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
