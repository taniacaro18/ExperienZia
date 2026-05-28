package com.experienzia.security;

import com.experienzia.entity.Estado;
import com.experienzia.entity.Rol;
import com.experienzia.entity.Usuario;
import com.experienzia.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import jakarta.annotation.Nonnull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// En cada request leo el Bearer, valido y dejo al usuario "logueado" para el resto del API
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UsuarioRepository usuarioRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
		this.jwtService = jwtService;
		this.usuarioRepository = usuarioRepository;
	}

	// OPTIONS del CORS y rutas públicas no me meto
	@Override
	protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
		return "OPTIONS".equalsIgnoreCase(request.getMethod())
				|| SecurityPaths.isPublic(request.getRequestURI());
	}

	@Override
	protected void doFilterInternal(
			@Nonnull HttpServletRequest request,
			@Nonnull HttpServletResponse response,
			@Nonnull FilterChain filterChain) throws ServletException, IOException {

		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		// Sin Bearer dejo pasar; SecurityConfig manda 401 si la ruta no es pública
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(7).trim();
		boolean rutaProtegida = !SecurityPaths.isPublic(request.getRequestURI());
		try {
			Long userId = jwtService.extractUserId(token);
			Usuario usuario = usuarioRepository.findById(userId).orElse(null);
			// Cuenta desactivada o borrada: token válido pero no dejo entrar
			if (usuario == null || usuario.getEstado() != Estado.ACTIVO) {
				SecurityContextHolder.clearContext();
				if (rutaProtegida) {
					responderNoAutorizado(response, "Sesión inválida o cuenta inactiva.");
					return;
				}
				filterChain.doFilter(request, response);
				return;
			}
			String rol = usuario.getRol() != null ? usuario.getRol().name() : Rol.ASISTENTE.name();
			var auth = new UsernamePasswordAuthenticationToken(
					usuario.getId(),
					null,
					List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception ignored) {
			// Token vencido o mal firmado
			SecurityContextHolder.clearContext();
			if (rutaProtegida) {
				responderNoAutorizado(response, "Token inválido o expirado.");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private static void responderNoAutorizado(HttpServletResponse response, String mensaje) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("text/plain;charset=UTF-8");
		response.getWriter().write(mensaje);
	}
}
