package com.experienzia.security;

import com.experienzia.entity.Rol;
import com.experienzia.exceptions.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

// Validaciones de rol que uso en controladores (export admin, etc.)
public final class SecurityAccessHelper {

	private SecurityAccessHelper() {
	}

	public static void requireRol(Rol rol) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new CustomException("Debes iniciar sesión.", HttpStatus.UNAUTHORIZED);
		}
		String expected = "ROLE_" + rol.name();
		boolean ok = auth.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(expected::equals);
		if (!ok) {
			throw new CustomException("No tienes permiso para esta operación.", HttpStatus.FORBIDDEN);
		}
	}
}
