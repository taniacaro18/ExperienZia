package com.experienzia.security;

// Rutas que pasan sin Bearer; tiene que coincidir con SecurityConfig o el filtro y la config pelean
public final class SecurityPaths {

	private SecurityPaths() {
	}

	public static boolean isPublic(String uri) {
		if (uri == null) {
			return false;
		}
		if (uri.startsWith("/uploads/")) {
			return true;
		}
		if (uri.startsWith("/actuator/health") || uri.startsWith("/actuator/info")) {
			return true;
		}
		if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
			return true;
		}
		if ("/swagger-ui.html".equals(uri)) {
			return true;
		}
		// Validar certificado y bajar PDF sin login
		if (uri.startsWith("/api/certificados/validar/") || uri.startsWith("/api/certificados/pdf/")) {
			return true;
		}
		if (uri.startsWith("/api/eventos/catalogo/publicos")) {
			return true;
		}
		return uri.startsWith("/api/usuarios/login")
				|| uri.startsWith("/api/usuarios/registro")
				|| uri.startsWith("/api/usuarios/recuperar");
	}
}
