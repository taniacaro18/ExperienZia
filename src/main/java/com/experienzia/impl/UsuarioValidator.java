package com.experienzia.impl;

import com.experienzia.dto.UsuarioDTO;
import com.experienzia.exceptions.CustomException;
import com.experienzia.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

// Validaciones de registro y perfil antes de guardar usuarios en la BD.
@Component
public class UsuarioValidator {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    // Nombre del formulario público: solo letras (incluye tildes) y espacios — la regex me costó un rato.
    private static final Pattern REG_NOMBRE_REGISTRO = Pattern.compile("^[\\p{L}\\s]+$");
    private static final String TELEFONO_PERFIL_REGEX = "^[0-9+\\s().-]+$";

    private final UsuarioRepository usuarioRepository;

    public UsuarioValidator(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Lo mínimo antes de intentar guardar en la BD (registro o crear staff).
    public void validarDatosObligatorios(String nombre, String email, String password) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new CustomException("El nombre es un campo obligatorio.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new CustomException("La contraseña es un campo obligatorio.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new CustomException("El correo electrónico es un campo obligatorio.");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new CustomException("El formato del correo electrónico no es válido.");
        }
    }

    // Reglas del HU de registro: celular 10 dígitos, doc numérico, password fuerte, etc.
    public void validarFormatoRegistro(UsuarioDTO dto) {
        String nombre = dto.getNombre().trim();
        if (nombre.length() < 3) {
            throw new CustomException("El nombre debe tener al menos 3 caracteres.");
        }
        if (!REG_NOMBRE_REGISTRO.matcher(nombre).matches()) {
            throw new CustomException("El nombre solo puede contener letras y espacios.");
        }
        String tel = dto.getTelefono() != null ? dto.getTelefono().trim() : "";
        if (tel.isEmpty()) {
            throw new CustomException("El número de celular es obligatorio.");
        }
        // Colombia: celular de 10 dígitos; otro regex aparte del de perfil.
        if (!tel.matches("^\\d{10}$")) {
            throw new CustomException("El celular debe tener exactamente 10 dígitos numéricos.");
        }
        String doc = dto.getNumeroDocumento() != null ? dto.getNumeroDocumento().trim() : "";
        if (doc.isEmpty()) {
            throw new CustomException("El número de documento es obligatorio.");
        }
        if (!doc.matches("^\\d{4,10}$")) {
            throw new CustomException("El documento solo puede contener números (entre 4 y 10 dígitos).");
        }
        if (dto.getTipoDocumento() == null || dto.getTipoDocumento().isBlank()) {
            throw new CustomException("El tipo de documento es obligatorio.");
        }
        String pwd = dto.getPassword();
        if (pwd.length() < 9) {
            throw new CustomException("La contraseña debe tener más de 8 caracteres (mínimo 9).");
        }
        // Reviso mayúscula, minúscula y especial con codePoints — me confundí un rato pero funciona.
        boolean mayus = pwd.codePoints().anyMatch(Character::isUpperCase);
        boolean minus = pwd.codePoints().anyMatch(Character::isLowerCase);
        boolean especial = pwd.codePoints()
                .anyMatch(cp -> !Character.isLetter(cp) && !Character.isDigit(cp) && !Character.isWhitespace(cp));
        if (!mayus) {
            throw new CustomException("La contraseña debe incluir al menos una letra mayúscula.");
        }
        if (!minus) {
            throw new CustomException("La contraseña debe incluir al menos una letra minúscula.");
        }
        if (!especial) {
            throw new CustomException("La contraseña debe incluir al menos un carácter especial.");
        }
    }

    // Consulto la BD para no duplicar correos en registro.
    public void validarEmailUnico(String email) {
        if (usuarioRepository.existsByEmail(email.trim().toLowerCase(Locale.ROOT))) {
            throw new CustomException("El correo electrónico ya se encuentra registrado.");
        }
    }

    // Solo el mismo usuario puede cambiar su password desde perfil (no el admin por este endpoint).
    public void validarActorPuedeCambiarContrasenaDe(Long usuarioObjetivoId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long actorId)) {
            throw new CustomException("No autenticado.", HttpStatus.UNAUTHORIZED);
        }
        if (!actorId.equals(usuarioObjetivoId)) {
            throw new CustomException(
                    "Solo puedes cambiar tu propia contraseña desde tu perfil.",
                    HttpStatus.FORBIDDEN);
        }
    }

    public void validarNumeroDocumentoUnico(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            return;
        }
        if (usuarioRepository.existsByNumeroDocumento(numeroDocumento.trim())) {
            throw new CustomException("El número de documento ya se encuentra registrado.");
        }
    }

    public void validarTelefonoUnico(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return;
        }
        if (usuarioRepository.existsByTelefono(telefono.trim())) {
            throw new CustomException("El teléfono ya se encuentra registrado.");
        }
    }

    // Perfil: formato más laxo que el celular de 10 dígitos del registro.
    public void validarTelefonoPerfil(String tel, String telefonoActual) {
        if (tel.length() < 7 || tel.length() > 20) {
            throw new CustomException("El teléfono debe tener entre 7 y 20 caracteres.");
        }
        if (!tel.matches(TELEFONO_PERFIL_REGEX)) {
            throw new CustomException(
                    "El teléfono solo puede incluir dígitos, espacios y los símbolos + ( ) - .");
        }
        if (!tel.equals(telefonoActual) && usuarioRepository.existsByTelefono(tel)) {
            throw new CustomException("El teléfono ya pertenece a otro usuario.");
        }
    }

    public void validarLongitudNuevaPasswordPerfil(String nuevaPass) {
        if (nuevaPass.length() < 4) {
            throw new CustomException("La contraseña debe tener al menos 4 caracteres.");
        }
    }
}
