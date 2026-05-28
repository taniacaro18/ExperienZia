package com.experienzia.impl;

import com.experienzia.dto.ActualizarPerfilDTO;
import com.experienzia.dto.CrearStaffDTO;
import com.experienzia.dto.LoginDTO;
import com.experienzia.dto.RecuperarPasswordDTO;
import com.experienzia.dto.RecuperarPasswordResponseDTO;
import com.experienzia.dto.UsuarioDTO;
import com.experienzia.entity.Estado;
import com.experienzia.entity.Rol;
import com.experienzia.entity.TipoNotificacion;
import com.experienzia.entity.Usuario;
import com.experienzia.exceptions.CustomException;
import com.experienzia.repository.UsuarioRepository;
import com.experienzia.service.NotificacionService;
import com.experienzia.service.UsuarioService;
import com.experienzia.spec.UsuarioSpecification;
import com.experienzia.spec.UsuarioSpecification.UsuarioSearchCriteria;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
// Aquí lo que hago es todo lo de cuentas: registro, login, staff del organizador y que el admin apruebe organizadores.
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;
    private final ModelMapper modelMapper;
    private final UsuarioValidator usuarioValidator;
    private final PasswordCryptoHelper passwordCrypto;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            NotificacionService notificacionService,
            ModelMapper modelMapper,
            UsuarioValidator usuarioValidator,
            PasswordCryptoHelper passwordCrypto) {
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
        this.modelMapper = modelMapper;
        this.usuarioValidator = usuarioValidator;
        this.passwordCrypto = passwordCrypto;
    }

    @Override
    public UsuarioDTO registrar(UsuarioDTO dto) {
        // Primero valido formato y unicidad; si algo falla mando error antes de tocar la BD.
        usuarioValidator.validarDatosObligatorios(dto.getNombre(), dto.getEmail(), dto.getPassword());
        usuarioValidator.validarFormatoRegistro(dto);
        usuarioValidator.validarEmailUnico(dto.getEmail());
        usuarioValidator.validarNumeroDocumentoUnico(dto.getNumeroDocumento());
        usuarioValidator.validarTelefonoUnico(dto.getTelefono());

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT));
        usuario.setPassword(passwordCrypto.encode(dto.getPassword()));
        usuario.setTelefono(blankToNull(dto.getTelefono()));
        usuario.setTipoDocumento(dto.getTipoDocumento());
        usuario.setNumeroDocumento(blankToNull(dto.getNumeroDocumento()));

        // El front manda tipo ASISTENTE u ORGANIZADOR; organizador queda PENDIENTE hasta que el admin lo apruebe.
        // El front manda tipo ASISTENTE u ORGANIZADOR; ADMIN/STAFF no se crean por registro abierto.
        String tipo = dto.getTipo();
        if ("ORGANIZADOR".equalsIgnoreCase(tipo)) {
            usuario.setRol(Rol.ORGANIZADOR);
            usuario.setEstado(Estado.PENDIENTE);
        } else if ("ASISTENTE".equalsIgnoreCase(tipo) || tipo == null) {
            usuario.setRol(Rol.ASISTENTE);
            usuario.setEstado(Estado.ACTIVO);
        } else {
            throw new CustomException(
                    "El campo 'tipo' debe ser ASISTENTE u ORGANIZADOR. ADMIN y STAFF están bloqueados.");
        }

        return toDto(usuarioRepository.save(usuario));
    }

    @Override
    // Login: si la clave vieja estaba en texto plano, la migro a BCrypt al entrar.
    public UsuarioDTO login(LoginDTO dto) {
        // Login normal: busco por email en minúsculas y comparo contraseña con el helper (BCrypt o legado).
        if (dto.getEmail() == null || dto.getPassword() == null) {
            throw new CustomException("Email y contraseña son obligatorios.");
        }
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new CustomException(
                        "No existe una cuenta registrada con este correo.", HttpStatus.UNAUTHORIZED));

        if (!passwordCrypto.passwordCoincide(dto.getPassword(), usuario.getPassword())) {
            throw new CustomException("La contraseña ingresada es incorrecta.", HttpStatus.UNAUTHORIZED);
        }
        if (usuario.getEstado() != Estado.ACTIVO) {
            throw new CustomException(
                    "Acceso denegado. El estado de la cuenta es: " + usuario.getEstado(),
                    HttpStatus.FORBIDDEN);
        }
        // Si en la BD quedó texto plano de datos viejos, en el primer login lo paso a hash y guardo.
        // Si aún tenía password vieja en texto plano, la migro a BCrypt en este mismo login.
        if (!passwordCrypto.passwordEsBcrypt(usuario.getPassword())) {
            usuario.setPassword(passwordCrypto.encode(dto.getPassword()));
            usuario = usuarioRepository.save(usuario);
        }
        return toDto(usuario);
    }

    @Override
    // El organizador crea su equipo STAFF ligado a su organizadorId.
    public UsuarioDTO crearStaff(CrearStaffDTO dto) {
        // Solo el organizador dueño puede crear STAFF ligado a su organizadorId.
        if (dto.getOrganizadorId() == null) {
            throw new CustomException("organizadorId es obligatorio.");
        }
        Usuario organizador = usuarioRepository.findById(dto.getOrganizadorId())
                .orElseThrow(() -> new CustomException(
                        "No se encontró el organizador con ID: " + dto.getOrganizadorId(),
                        HttpStatus.NOT_FOUND));
        if (organizador.getRol() != Rol.ORGANIZADOR) {
            throw new CustomException("Solo un ORGANIZADOR puede crear usuarios STAFF.",
                    HttpStatus.FORBIDDEN);
        }
        usuarioValidator.validarDatosObligatorios(dto.getNombre(), dto.getEmail(), dto.getPassword());
        usuarioValidator.validarEmailUnico(dto.getEmail());
        usuarioValidator.validarNumeroDocumentoUnico(dto.getNumeroDocumento());
        usuarioValidator.validarTelefonoUnico(dto.getTelefono());

        Usuario staff = new Usuario();
        staff.setNombre(dto.getNombre().trim());
        staff.setEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT));
        staff.setPassword(passwordCrypto.encode(dto.getPassword()));
        staff.setTelefono(dto.getTelefono());
        staff.setTipoDocumento(dto.getTipoDocumento());
        staff.setNumeroDocumento(dto.getNumeroDocumento());
        staff.setRol(Rol.STAFF);
        staff.setEstado(Estado.ACTIVO);
        staff.setOrganizadorId(dto.getOrganizadorId());

        return toDto(usuarioRepository.save(staff));
    }

    // --- Ciclo de vida organizador (admin) ---

    @Override
    public UsuarioDTO aprobarOrganizador(Long id) {
        // Flujo admin: organizador PENDIENTE → ACTIVO para que pueda crear eventos.
        Usuario u = buscarOFallar(id);
        if (u.getRol() != Rol.ORGANIZADOR) {
            throw new CustomException("Solo se pueden aprobar usuarios con rol ORGANIZADOR.");
        }
        if (u.getEstado() != Estado.PENDIENTE) {
            throw new CustomException(
                    "El usuario no está en estado PENDIENTE. Estado actual: " + u.getEstado());
        }
        u.setEstado(Estado.ACTIVO);
        return toDto(usuarioRepository.save(u));
    }

    @Override
    public UsuarioDTO rechazarOrganizador(Long id) {
        Usuario u = buscarOFallar(id);
        if (u.getRol() != Rol.ORGANIZADOR) {
            throw new CustomException("Solo se pueden rechazar usuarios con rol ORGANIZADOR.");
        }
        if (u.getEstado() != Estado.PENDIENTE) {
            throw new CustomException(
                    "El usuario no está en estado PENDIENTE. Estado actual: " + u.getEstado());
        }
        u.setEstado(Estado.RECHAZADO);
        return toDto(usuarioRepository.save(u));
    }

    @Override
    public UsuarioDTO desactivar(Long id) {
        Usuario u = buscarOFallar(id);
        if (u.getEstado() == Estado.INACTIVO) {
            throw new CustomException("El usuario ya se encuentra desactivado.");
        }
        u.setEstado(Estado.INACTIVO);
        return toDto(usuarioRepository.save(u));
    }

    @Override
    public UsuarioDTO reactivar(Long id) {
        Usuario u = buscarOFallar(id);
        if (u.getEstado() == Estado.ACTIVO) {
            throw new CustomException("El usuario ya se encuentra ACTIVO.");
        }
        if (u.getEstado() != Estado.INACTIVO) {
            throw new CustomException(
                    "Solo se puede reactivar una cuenta en estado INACTIVO. Estado actual: " + u.getEstado() + ".");
        }
        u.setEstado(Estado.ACTIVO);
        return toDto(usuarioRepository.save(u));
    }

    @Override
    public UsuarioDTO desactivarStaffPorOrganizador(Long organizadorId, Long staffId) {
        Usuario staff = validarStaffDelOrganizador(organizadorId, staffId);
        if (staff.getEstado() == Estado.INACTIVO) {
            throw new CustomException("El staff ya se encuentra desactivado.");
        }
        staff.setEstado(Estado.INACTIVO);
        return toDto(usuarioRepository.save(staff));
    }

    @Override
    public UsuarioDTO reactivarStaffPorOrganizador(Long organizadorId, Long staffId) {
        Usuario staff = validarStaffDelOrganizador(organizadorId, staffId);
        if (staff.getEstado() == Estado.ACTIVO) {
            throw new CustomException("El staff ya se encuentra ACTIVO.");
        }
        if (staff.getEstado() != Estado.INACTIVO) {
            throw new CustomException(
                    "Solo se puede reactivar un staff en estado INACTIVO. Estado actual: " + staff.getEstado() + ".");
        }
        staff.setEstado(Estado.ACTIVO);
        return toDto(usuarioRepository.save(staff));
    }

    // Me aseguro de que el staff sea de ese organizador y no de otro (check-in y asignaciones).
    // El organizador solo puede activar/desactivar staff que él mismo creó.
    private Usuario validarStaffDelOrganizador(Long organizadorId, Long staffId) {
        if (organizadorId == null) {
            throw new CustomException("organizadorId es obligatorio.");
        }
        Usuario organizador = buscarOFallar(organizadorId);
        if (organizador.getRol() != Rol.ORGANIZADOR) {
            throw new CustomException("Solo un ORGANIZADOR puede gestionar el estado de su staff.",
                    HttpStatus.FORBIDDEN);
        }
        Usuario staff = buscarOFallar(staffId);
        if (staff.getRol() != Rol.STAFF) {
            throw new CustomException("El usuario indicado no es STAFF.", HttpStatus.BAD_REQUEST);
        }
        if (staff.getOrganizadorId() == null || !staff.getOrganizadorId().equals(organizadorId)) {
            throw new CustomException("El STAFF no pertenece a este organizador.", HttpStatus.FORBIDDEN);
        }
        return staff;
    }

    @Override
    public UsuarioDTO cambiarRol(Long id, String nuevoRol) {
        if (nuevoRol == null || nuevoRol.isBlank()) {
            throw new CustomException("El rol es obligatorio.");
        }
        Usuario u = buscarOFallar(id);
        Rol rol;
        try {
            rol = Rol.valueOf(nuevoRol.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CustomException("Rol no válido. Use ADMIN, ORGANIZADOR, ASISTENTE o STAFF.");
        }
        if (rol == Rol.STAFF && u.getOrganizadorId() == null) {
            throw new CustomException(
                    "No se puede convertir un usuario en STAFF sin asociarlo a un organizador.");
        }
        u.setRol(rol);
        return toDto(usuarioRepository.save(u));
    }

    @Override
    public UsuarioDTO obtenerPorId(Long id) {
        return toDto(buscarOFallar(id));
    }

    @Override
    public UsuarioDTO actualizarPerfil(Long id, ActualizarPerfilDTO dto) {
        // Perfil: teléfono opcional; contraseña solo si el mismo usuario está logueado (lo valida el validator).
        Usuario u = buscarOFallar(id);
        if (dto.getTelefono() != null) {
            String tel = dto.getTelefono().isBlank() ? null : dto.getTelefono().trim();
            if (tel != null) {
                usuarioValidator.validarTelefonoPerfil(tel, u.getTelefono());
            }
            u.setTelefono(tel);
        }
        if (dto.getNuevaPassword() != null && !dto.getNuevaPassword().isBlank()) {
            usuarioValidator.validarActorPuedeCambiarContrasenaDe(id);
            String nuevaPass = dto.getNuevaPassword().trim();
            usuarioValidator.validarLongitudNuevaPasswordPerfil(nuevaPass);
            passwordCrypto.validarNuevaPasswordDistintaDeAnterior(nuevaPass, u.getPassword());
            u.setPassword(passwordCrypto.encode(nuevaPass));
        }
        return toDto(usuarioRepository.save(u));
    }

    @Override
    // Olvidé mi clave: valido email + documento y devuelvo temporal en la respuesta (el front la muestra).
    public RecuperarPasswordResponseDTO recuperarPassword(RecuperarPasswordDTO dto) {
        // Recuperación pública: email + documento deben coincidir con la cuenta ACTIVA.
        // Recuperación: email + documento deben coincidir; genero clave temporal y la devuelvo en el response (HU).
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new CustomException("El correo electrónico es obligatorio.");
        }
        if (dto.getNumeroDocumento() == null || dto.getNumeroDocumento().isBlank()) {
            throw new CustomException("El número de documento es obligatorio.");
        }
        Usuario u = usuarioRepository.findByEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new CustomException(
                        "No existe un usuario con ese correo electrónico.", HttpStatus.NOT_FOUND));
        if (u.getEstado() != Estado.ACTIVO) {
            throw new CustomException(
                    "Solo cuentas activas pueden recuperar contraseña. Contacta al administrador.",
                    HttpStatus.FORBIDDEN);
        }
        if (u.getNumeroDocumento() == null
                || !u.getNumeroDocumento().trim().equals(dto.getNumeroDocumento().trim())) {
            throw new CustomException(
                    "Los datos de identidad no coinciden con la cuenta.", HttpStatus.UNAUTHORIZED);
        }
        String temporal = passwordCrypto.generarPasswordTemporal(10);
        u.setPassword(passwordCrypto.encode(temporal));
        Usuario guardado = usuarioRepository.save(u);

        return new RecuperarPasswordResponseDTO(
                guardado.getId(),
                guardado.getEmail(),
                temporal,
                "Se generó una contraseña temporal. Cámbiala desde tu perfil.");
    }

    // Admin/organizador: genero pass temporal (a veces el mismo doc) y aviso por notificación.
    @Override
    public RecuperarPasswordResponseDTO reenviarCredenciales(Long usuarioId) {
        // Admin/organizador reenvía credenciales: uso el doc como pass o temporal si no hay doc.
        Usuario u = buscarOFallar(usuarioId);
        if (u.getEstado() != Estado.ACTIVO) {
            throw new CustomException(
                    "Solo se pueden reenviar credenciales a usuarios ACTIVOS. Estado actual: " + u.getEstado() + ".",
                    HttpStatus.FORBIDDEN);
        }
        // Atajo: si tiene documento, la clave inicial es el doc; si no, random sin caracteres confusos.
        String nuevaPass = (u.getNumeroDocumento() != null && !u.getNumeroDocumento().isBlank())
                ? u.getNumeroDocumento().trim()
                : passwordCrypto.generarPasswordTemporal(10);
        u.setPassword(passwordCrypto.encode(nuevaPass));
        Usuario guardado = usuarioRepository.save(u);

        notificacionService.crear(guardado.getId(),
                "Se reenviaron tus credenciales. Tu nueva contraseña inicial es: " + nuevaPass
                        + ". Cámbiala desde tu perfil.",
                TipoNotificacion.INFO);

        return new RecuperarPasswordResponseDTO(
                guardado.getId(),
                guardado.getEmail(),
                nuevaPass,
                "Credenciales reenviadas. La contraseña temporal fue notificada al usuario.");
    }

    @Override
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioDTO> buscarPorCriterios(UsuarioSearchCriteria c) {
        // Filtros del panel admin: armo Specification con AND para no traer todo el mundo.
        Specification<Usuario> spec = Specification.where(UsuarioSpecification.hasNombre(c.getNombre()))
                .and(UsuarioSpecification.hasEmail(c.getEmail()))
                .and(UsuarioSpecification.hasRol(c.getRol()))
                .and(UsuarioSpecification.hasEstado(c.getEstado()))
                .and(UsuarioSpecification.hasOrganizadorId(c.getOrganizadorId()));
        return usuarioRepository.findAll(spec).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private Usuario buscarOFallar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "No se encontró un usuario con el ID: " + id, HttpStatus.NOT_FOUND));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private UsuarioDTO toDto(Usuario u) {
        UsuarioDTO dto = modelMapper.map(u, UsuarioDTO.class);
        if (u.getRol() != null) {
            dto.setRol(u.getRol().name());
        }
        if (u.getEstado() != null) {
            dto.setEstado(u.getEstado().name());
        }
        dto.setPassword(null); // nunca devuelvo el hash al front
        return dto;
    }
}
