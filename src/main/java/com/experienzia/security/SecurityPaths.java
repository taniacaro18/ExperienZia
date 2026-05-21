package com.experienzia.security;

/**
 * Lista de rutas HTTP públicas (sin JWT).
 * Tiene que coincidir con lo que permitimos en SecurityConfig.
 */
public final class SecurityPaths {

	// clase utilitaria: no se instancia, solo métodos static
	private SecurityPaths() {
	}

	/**
	 * Devuelve true si esa URI puede pasar sin token Bearer.
	 *
	 * @param uri ruta de la petición, ej. /api/usuarios/login
	 */
	public static boolean isPublic(String uri) {
		if (uri == null) {
			return false;
		}
		// archivos subidos (comprobantes, etc.)
		if (uri.startsWith("/uploads/")) {
			return true;
		}
		// health check de Spring Actuator
		if (uri.startsWith("/actuator/health") || uri.startsWith("/actuator/info")) {
			return true;
		}
		// documentación Swagger
		if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
			return true;
		}
		if ("/swagger-ui.html".equals(uri)) {
			return true;
		}
		// validar certificado y descargar PDF sin estar logueado
		if (uri.startsWith("/api/certificados/validar/") || uri.startsWith("/api/certificados/pdf/")) {
			return true;
		}
		// catálogo público de eventos
		if (uri.startsWith("/api/eventos/catalogo/publicos")) {
			return true;
		}
		// login, registro y recuperar contraseña
		return uri.startsWith("/api/usuarios/login")
				|| uri.startsWith("/api/usuarios/registro")
				|| uri.startsWith("/api/usuarios/recuperar");
	}
}
