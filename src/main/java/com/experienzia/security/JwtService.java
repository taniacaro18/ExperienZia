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

/**
 * Servicio para crear y validar tokens JWT (JSON Web Token).
 * El login devuelve un token y el cliente lo manda en Authorization: Bearer ...
 */
@Service
public class JwtService {

	// clave secreta para firmar el token (no se debe filtrar)
	private final SecretKey key;
	// cuántos milisegundos dura el token antes de expirar
	private final long expirationMs;

	public JwtService(
			@Value("${experienzia.jwt.secret}") String secret,
			@Value("${experienzia.jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(deriveKeyBytes(secret));
		this.expirationMs = expirationMs;
	}

	/**
	 * Prepara bytes válidos para HS256: si el secreto es corto hacemos SHA-256,
	 * si es largo puede ser Base64 de 32+ bytes.
	 */
	private static byte[] deriveKeyBytes(String secret) {
		String s = secret != null ? secret.trim() : "";
		if (s.length() >= 64) {
			try {
				return Decoders.BASE64.decode(s);
			} catch (IllegalArgumentException ignored) {
				// no era Base64 válido, seguimos con texto plano abajo
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

	/**
	 * Genera un JWT nuevo con el id del usuario, email y rol.
	 */
	public String generateToken(Long userId, String email, String rol) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(String.valueOf(userId)) // "subject" = quién es el dueño del token
				.claim("email", email)
				.claim("rol", rol)
				.issuedAt(now)
				.expiration(exp)
				.signWith(key)
				.compact();
	}

	/**
	 * Lee y verifica el token; si la firma no cuadra lanza excepción.
	 */
	public Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	/**
	 * Saca el id de usuario del subject del JWT.
	 */
	public Long extractUserId(String token) {
		String sub = parseClaims(token).getSubject();
		return Long.parseLong(sub);
	}
}
