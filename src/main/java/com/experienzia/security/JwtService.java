package com.experienzia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

// Creo y valido el token que el front manda en Authorization: Bearer ...
@Service
public class JwtService {

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(
			@Value("${experienzia.jwt.secret}") String secret,
			@Value("${experienzia.jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(deriveKeyBytes(secret));
		this.expirationMs = expirationMs;
	}

	// Si el secreto es corto lo estiro con SHA-256; si es largo puede ser Base64
	private static byte[] deriveKeyBytes(String secret) {
		String s = secret != null ? secret.trim() : "";
		if (s.length() >= 64) {
			try {
				return Decoders.BASE64.decode(s);
			} catch (IllegalArgumentException ignored) {
			}
		}
		byte[] raw = s.getBytes(StandardCharsets.UTF_8);
		if (raw.length >= 32) {
			return raw;
		}
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return md.digest(raw);
		} catch (Exception e) {
			throw new IllegalStateException("No se pudo derivar la clave JWT", e);
		}
	}

	public String generateToken(Long userId, String email, String rol) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("email", email)
				.claim("rol", rol)
				.issuedAt(now)
				.expiration(exp)
				.signWith(key)
				.compact();
	}

	// Leo el token cuando llega una petición; si está vencido revienta y el filtro limpia contexto
	public Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public Long extractUserId(String token) {
		String sub = parseClaims(token).getSubject();
		return Long.parseLong(sub);
	}
}
