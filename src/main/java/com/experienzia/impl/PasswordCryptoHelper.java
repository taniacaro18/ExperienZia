package com.experienzia.impl;

import com.experienzia.exceptions.CustomException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

// Todo lo de contraseñas: hashear, comparar y generar temporales para recuperar cuenta.
@Component
public class PasswordCryptoHelper {

    // Sin 0/O ni 1/I para que no se confundan al leer la clave temporal en pantalla.
    private static final String ALFABETO_PASS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordEncoder passwordEncoder;

    public PasswordCryptoHelper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String encode(String raw) {
        return passwordEncoder.encode(raw);
    }

    // Clave aleatoria para recuperar cuenta o reenviar credenciales.
    public String generarPasswordTemporal(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(ALFABETO_PASS.charAt(RANDOM.nextInt(ALFABETO_PASS.length())));
        }
        return sb.toString();
    }

    // Reconozco si ya está en BCrypt ($2a$, $2b$…) o si quedó texto plano de datos viejos.
    public boolean passwordEsBcrypt(String stored) {
        return stored != null
                && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
    }

    // Si no es BCrypt comparo en claro (legado); en login migramos a hash nuevo.
    public boolean passwordCoincide(String raw, String stored) {
        if (stored == null || raw == null) {
            return false;
        }
        if (passwordEsBcrypt(stored)) {
            return passwordEncoder.matches(raw, stored);
        }
        return raw.equals(stored);
    }

    // Al cambiar desde perfil no puede ser la misma que ya tenía (aunque esté hasheada).
    public void validarNuevaPasswordDistintaDeAnterior(String nuevaPass, String storedHash) {
        if (passwordCoincide(nuevaPass, storedHash)) {
            throw new CustomException("La nueva contraseña no puede ser igual a la anterior.");
        }
    }
}
