package com.experienzia.util;

import jakarta.servlet.http.HttpServletRequest;

// Guardo en auditoría la IP real aunque haya proxy delante (nginx, etc.)
public final class ClientIpResolver {

	private ClientIpResolver() {
	}

	public static String resolve(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		// Detrás de nginx/proxy la IP real viene en X-Forwarded-For (la primera de la lista)
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		String realIp = request.getHeader("X-Real-IP");
		if (realIp != null && !realIp.isBlank()) {
			return realIp.trim();
		}
		return request.getRemoteAddr();
	}
}
