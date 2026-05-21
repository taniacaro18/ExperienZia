package com.experienzia.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Obtiene la IP real del cliente cuando hay proxy o balanceador delante.
 * Mira primero X-Forwarded-For y X-Real-IP, si no usa getRemoteAddr().
 */
public final class ClientIpResolver {

	private ClientIpResolver() {
	}

	/**
	 * Resuelve la IP desde la petición HTTP (para auditoría, logs, etc.).
	 */
	public static String resolve(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		// detrás de nginx/load balancer suele venir la cadena de IPs aquí
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			// la primera IP de la lista es la del cliente original
			return xff.split(",")[0].trim();
		}
		String realIp = request.getHeader("X-Real-IP");
		if (realIp != null && !realIp.isBlank()) {
			return realIp.trim();
		}
		// conexión directa sin proxy
		return request.getRemoteAddr();
	}
}
