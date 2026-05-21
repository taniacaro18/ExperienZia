package com.experienzia.controller;

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
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// Controlador principal de usuarios: registro, login, perfil, búsqueda, etc.
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

    // POST /api/usuarios/registro — crea un usuario nuevo (asistente u organizador según el DTO)
    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registrar(@RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.registrar(dto));
    }

    // POST /api/usuarios/login — valida email/contraseña y devuelve token JWT + datos del usuario
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO dto) {
        UsuarioDTO usuario = usuarioService.login(dto);
        // Aquí generamos el token porque el controlador coordina autenticación + respuesta HTTP
        String token = jwtService.generateToken(usuario.getId(), usuario.getEmail(), usuario.getRol());
        return ResponseEntity.ok(new LoginResponseDTO(token, usuario));
    }

    // POST /api/usuarios/staff — el organizador crea una cuenta de personal (staff)
    @PostMapping("/staff")
    public ResponseEntity<UsuarioDTO> crearStaff(@RequestBody CrearStaffDTO dto) {
        return ResponseEntity.ok(usuarioService.crearStaff(dto));
    }

    // GET /api/usuarios — lista todos los usuarios (típicamente uso admin)
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // GET /api/usuarios/buscar — filtros por query string; Spring rellena UsuarioSearchCriteria solo
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioDTO>> buscar(UsuarioSearchCriteria criteria) {
        return ResponseEntity.ok(usuarioService.buscarPorCriterios(criteria));
    }

    // GET /api/usuarios/{id} — obtiene un usuario por su id
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // PUT /api/usuarios/{id} — actualiza nombre, email u otros datos del perfil
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarPerfil(@PathVariable Long id,
                                                       @RequestBody ActualizarPerfilDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(id, dto));
    }

    // POST /api/usuarios/recuperar — flujo de olvidé mi contraseña
    @PostMapping("/recuperar")
    public ResponseEntity<RecuperarPasswordResponseDTO> recuperar(@RequestBody RecuperarPasswordDTO dto) {
        return ResponseEntity.ok(usuarioService.recuperarPassword(dto));
    }

    /**
     * Reenviar credenciales de un usuario (acción del organizador para asistentes cargados
     * masivamente que olvidaron su contraseña inicial).
     * Los administradores no pueden usar este endpoint (no restablecen contraseñas de terceros).
     */
    // POST /api/usuarios/{id}/reenviar-credenciales?actorId=...
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

    // Método privado de ayuda: mira el contexto de seguridad de Spring para saber si es ADMIN
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
