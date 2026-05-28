package com.experienzia.impl;

import com.experienzia.dto.AforoEnVivoDTO;
import com.experienzia.dto.AsistenteEventoDTO;
import com.experienzia.dto.EventoStaffDTO;
import com.experienzia.dto.FilaAsistenteCargaDTO;
import com.experienzia.dto.InscripcionDTO;
import com.experienzia.dto.ResultadoCargaAsistentesDTO;
import com.experienzia.dto.StaffAsignadoDTO;
import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.EstadoInscripcion;
import com.experienzia.entity.Estado;
import com.experienzia.entity.Evento;
import com.experienzia.entity.FuncionStaff;
import com.experienzia.entity.Inscripcion;
import com.experienzia.entity.Rol;
import com.experienzia.entity.TipoEvento;
import com.experienzia.entity.TipoNotificacion;
import com.experienzia.entity.Usuario;
import com.experienzia.exceptions.CustomException;
import com.experienzia.repository.EventoRepository;
import com.experienzia.repository.InscripcionRepository;
import com.experienzia.repository.UsuarioRepository;
import com.experienzia.service.InscripcionService;
import com.experienzia.service.NotificacionService;
import com.experienzia.service.StaffEventoService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
// Inscripciones, QR check-in/out, carga masiva CSV y aforo en vivo — el corazón del lado asistente/staff.
public class InscripcionServiceImpl implements InscripcionService {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AsistenteCsvParser asistenteCsvParser;
    private final InscripcionValidator inscripcionValidator;
    private final StaffEventoService staffEventoService;

    public InscripcionServiceImpl(
            InscripcionRepository inscripcionRepository,
            EventoRepository eventoRepository,
            UsuarioRepository usuarioRepository,
            NotificacionService notificacionService,
            ModelMapper modelMapper,
            PasswordEncoder passwordEncoder,
            AsistenteCsvParser asistenteCsvParser,
            InscripcionValidator inscripcionValidator,
            StaffEventoService staffEventoService) {
        this.inscripcionRepository = inscripcionRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.asistenteCsvParser = asistenteCsvParser;
        this.inscripcionValidator = inscripcionValidator;
        this.staffEventoService = staffEventoService;
    }

    @Override
    public InscripcionDTO inscribir(Long usuarioId, Long eventoId) {
        return inscribirInterno(usuarioId, eventoId, true);
    }

    // TX aparte: al aprobar pago inscribo al organizador sin tumbar la transacción del pago si falla el cupo.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InscripcionDTO inscribirOrganizadorEnSuEvento(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new CustomException("El evento no existe.", HttpStatus.NOT_FOUND));
        if (evento.getOrganizadorId() == null) {
            return null;
        }
        Long organizadorId = evento.getOrganizadorId();

        Optional<Inscripcion> existente = inscripcionRepository
                .findByUsuarioIdAndEventoId(organizadorId, eventoId);
        if (existente.isPresent() && existente.get().getEstado() != EstadoInscripcion.CANCELADO) {
            return toDto(existente.get());
        }

        if (evento.getAforoActual() >= evento.getAforoMaximo()) {
            return null;
        }
        evento.setAforoActual(evento.getAforoActual() + 1);
        eventoRepository.save(evento);

        Inscripcion ins;
        if (existente.isPresent()) {
            ins = existente.get();
            ins.setEstado(EstadoInscripcion.INSCRITO);
            ins.setFechaInscripcion(LocalDateTime.now());
            if (ins.getCodigoQR() == null) {
                ins.setCodigoQR(generarCodigoQR());
            }
        } else {
            ins = new Inscripcion();
            ins.setUsuarioId(organizadorId);
            ins.setEventoId(eventoId);
            ins.setFechaInscripcion(LocalDateTime.now());
            ins.setEstado(EstadoInscripcion.INSCRITO);
            ins.setCodigoQR(generarCodigoQR());
        }
        return toDto(inscripcionRepository.save(ins));
    }

    private InscripcionDTO inscribirInterno(Long usuarioId, Long eventoId, boolean autoInscripcion) {
        // autoInscripcion=false en carga CSV: eventos privados por invitación sin pedir login público.
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new CustomException("El evento no existe.", HttpStatus.NOT_FOUND));
        if (evento.getEstado() == EstadoEvento.CANCELADO) {
            throw new CustomException("No se puede inscribir a un evento CANCELADO.", HttpStatus.BAD_REQUEST);
        }
        if (evento.getEstado() != EstadoEvento.ACTIVO) {
            throw new CustomException("No se puede inscribir a un evento que no está ACTIVO.", HttpStatus.BAD_REQUEST);
        }
        // Público = el asistente se inscribe solo; privado = solo lista del organizador o CSV.
        if (autoInscripcion && evento.getTipoEvento() != TipoEvento.PUBLICO) {
            throw new CustomException(
                    "La auto-inscripción solo está habilitada para eventos PÚBLICOS. Los eventos privados son por invitación.",
                    HttpStatus.FORBIDDEN);
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new CustomException("El usuario no existe.", HttpStatus.NOT_FOUND));
        if (usuario.getEstado() != Estado.ACTIVO) {
            throw new CustomException("El usuario no está ACTIVO. Estado actual: " + usuario.getEstado() + ".",
                    HttpStatus.FORBIDDEN);
        }
        if (usuario.getRol() == Rol.ADMIN) {
            throw new CustomException("La cuenta de administrador no puede inscribirse como asistente.",
                    HttpStatus.FORBIDDEN);
        }
        if (evento.getOrganizadorId() != null && evento.getOrganizadorId().equals(usuarioId)) {
            throw new CustomException("El organizador del evento no puede inscribirse como asistente de su propio evento.",
                    HttpStatus.FORBIDDEN);
        }
        if (usuario.getRol() == Rol.STAFF && staffEventoService.existeAsignacion(usuarioId, eventoId)) {
            throw new CustomException("El STAFF asignado a este evento no puede inscribirse también como asistente.",
                    HttpStatus.FORBIDDEN);
        }

        if (evento.getAforoActual() >= evento.getAforoMaximo()) {
            throw new CustomException("El evento ya no tiene cupo disponible.", HttpStatus.CONFLICT);
        }

        Optional<Inscripcion> existente = inscripcionRepository.findByUsuarioIdAndEventoId(usuarioId, eventoId);
        if (existente.isPresent()) {
            Inscripcion previa = existente.get();
            if (previa.getEstado() != EstadoInscripcion.CANCELADO) {
                throw new CustomException("El usuario ya se encuentra inscrito en este evento.", HttpStatus.CONFLICT);
            }
            evento.setAforoActual(evento.getAforoActual() + 1);
            eventoRepository.save(evento);
            previa.setFechaInscripcion(LocalDateTime.now());
            previa.setEstado(EstadoInscripcion.INSCRITO);
            if (previa.getCodigoQR() == null) {
                previa.setCodigoQR(generarCodigoQR());
            }
            return toDto(inscripcionRepository.save(previa));
        }

        evento.setAforoActual(evento.getAforoActual() + 1);
        eventoRepository.save(evento);

        Inscripcion nueva = new Inscripcion();
        nueva.setUsuarioId(usuarioId);
        nueva.setEventoId(eventoId);
        nueva.setFechaInscripcion(LocalDateTime.now());
        nueva.setEstado(EstadoInscripcion.INSCRITO);
        nueva.setCodigoQR(generarCodigoQR());
        return toDto(inscripcionRepository.save(nueva));
    }

    // Código único por inscripción; el staff lo escanea en puerta (sin guiones para que sea más corto).
    private String generarCodigoQR() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public InscripcionDTO cancelar(Long inscripcionId) {
        Inscripcion ins = buscarInscripcion(inscripcionId);
        if (ins.getEstado() == EstadoInscripcion.CANCELADO) {
            throw new CustomException("La inscripción ya está cancelada.", HttpStatus.BAD_REQUEST);
        }
        if (ins.getEstado() == EstadoInscripcion.ASISTIO) {
            throw new CustomException("No se puede cancelar una inscripción si ya se marcó asistencia.", HttpStatus.BAD_REQUEST);
        }
        ins.setEstado(EstadoInscripcion.CANCELADO);

        Evento evento = eventoRepository.findById(ins.getEventoId())
                .orElseThrow(() -> new CustomException("El evento asociado a la inscripción no existe.", HttpStatus.NOT_FOUND));
        if (evento.getAforoActual() > 0) {
            evento.setAforoActual(evento.getAforoActual() - 1);
            eventoRepository.save(evento);
        }
        return toDto(inscripcionRepository.save(ins));
    }

    @Override
    public InscripcionDTO checkIn(Long inscripcionId, Long staffUsuarioId) {
        // Manual o vía QR: valido staff, ventana horaria y que no se pase del aforo con gente dentro.
        Inscripcion ins = buscarInscripcion(inscripcionId);
        Evento evento = eventoRepository.findById(ins.getEventoId())
                .orElseThrow(() -> new CustomException("Evento no encontrado.", HttpStatus.NOT_FOUND));
        staffEventoService.validarStaffAsignado(staffUsuarioId, evento);
        inscripcionValidator.validarEventoActivoYEnFecha(evento, "registrar asistencia");

        if (ins.getEstado() == EstadoInscripcion.CANCELADO) {
            throw new CustomException("No se puede marcar asistencia en una inscripción cancelada.", HttpStatus.BAD_REQUEST);
        }
        if (ins.getFechaCheckIn() != null) {
            throw new CustomException(
                    "Este QR ya fue utilizado. Primer ingreso registrado a las "
                            + ins.getFechaCheckIn().toLocalTime().withNano(0) + ".",
                    HttpStatus.BAD_REQUEST);
        }
        if (evento.getAforoActual() != 0 && evento.getAforoMaximo() > 0) {
            long presentes = inscripcionRepository.findByEventoId(evento.getId()).stream()
                    .filter(i -> i.getEstado() == EstadoInscripcion.ASISTIO && i.getFechaCheckOut() == null)
                    .count();
            if (presentes >= evento.getAforoMaximo()) {
                throw new CustomException("Aforo máximo alcanzado. No se permiten más ingresos.",
                        HttpStatus.CONFLICT);
            }
        }
        ins.setEstado(EstadoInscripcion.ASISTIO);
        ins.setFechaCheckIn(LocalDateTime.now());
        Inscripcion guardada = inscripcionRepository.save(ins);

        notificacionService.crear(ins.getUsuarioId(),
                "Tu asistencia al evento \"" + evento.getNombre() + "\" fue registrada.",
                TipoNotificacion.INFO);
        InscripcionDTO dto = toDto(guardada);
        enriquecerDatosCheckInEnDto(dto, ins.getUsuarioId(), ins.getEventoId());
        return dto;
    }

    @Override
    public InscripcionDTO checkInPorQR(String codigoQR, Long staffUsuarioId, Long eventoId) {
        if (codigoQR == null || codigoQR.isBlank()) {
            throw new CustomException("Código QR vacío.", HttpStatus.BAD_REQUEST);
        }
        Inscripcion ins = inscripcionRepository.findByCodigoQR(codigoQR.trim())
                .orElseThrow(() -> new CustomException("QR inválido o no registrado.", HttpStatus.NOT_FOUND));
        if (eventoId != null && !eventoId.equals(ins.getEventoId())) {
            throw new CustomException("El QR pertenece a otro evento.", HttpStatus.BAD_REQUEST);
        }
        return checkIn(ins.getId(), staffUsuarioId);
    }

    @Override
    public InscripcionDTO checkOut(Long inscripcionId, Long staffUsuarioId) {
        // Solo si ya hizo check-in; libero cupo “presente” para el aforo en vivo.
        Inscripcion ins = buscarInscripcion(inscripcionId);
        Evento evento = eventoRepository.findById(ins.getEventoId())
                .orElseThrow(() -> new CustomException("Evento no encontrado.", HttpStatus.NOT_FOUND));
        staffEventoService.validarStaffAsignado(staffUsuarioId, evento);
        inscripcionValidator.validarEventoActivoYEnFecha(evento, "registrar salida");

        if (ins.getEstado() != EstadoInscripcion.ASISTIO || ins.getFechaCheckIn() == null) {
            throw new CustomException("Solo se puede marcar salida si la asistencia fue confirmada.", HttpStatus.BAD_REQUEST);
        }
        if (ins.getFechaCheckOut() != null) {
            throw new CustomException(
                    "Este QR ya tiene salida registrada a las "
                            + ins.getFechaCheckOut().toLocalTime().withNano(0) + ".",
                    HttpStatus.BAD_REQUEST);
        }
        ins.setFechaCheckOut(LocalDateTime.now());
        Inscripcion guardada = inscripcionRepository.save(ins);

        notificacionService.crear(ins.getUsuarioId(),
                "Tu salida del evento \"" + evento.getNombre() + "\" fue registrada.",
                TipoNotificacion.INFO);
        InscripcionDTO dto = toDto(guardada);
        enriquecerDatosCheckInEnDto(dto, ins.getUsuarioId(), ins.getEventoId());
        return dto;
    }

    @Override
    public InscripcionDTO checkOutPorQR(String codigoQR, Long staffUsuarioId, Long eventoId) {
        if (codigoQR == null || codigoQR.isBlank()) {
            throw new CustomException("Código QR vacío.", HttpStatus.BAD_REQUEST);
        }
        Inscripcion ins = inscripcionRepository.findByCodigoQR(codigoQR.trim())
                .orElseThrow(() -> new CustomException("QR inválido o no registrado.", HttpStatus.NOT_FOUND));
        if (eventoId != null && !eventoId.equals(ins.getEventoId())) {
            throw new CustomException("El QR pertenece a otro evento.", HttpStatus.BAD_REQUEST);
        }
        return checkOut(ins.getId(), staffUsuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscripcionDTO> listarPorEvento(Long eventoId) {
        return inscripcionRepository.findByEventoId(eventoId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InscripcionDTO> listarPorUsuario(Long usuarioId) {
        return inscripcionRepository.findByUsuarioId(usuarioId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenteEventoDTO> listarAsistentesParaStaff(Long eventoId, Long staffUsuarioId, String busqueda) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new CustomException("No se encontró el evento con ID: " + eventoId, HttpStatus.NOT_FOUND));
        staffEventoService.validarStaffAsignado(staffUsuarioId, evento);
        return obtenerAsistentes(evento, busqueda);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenteEventoDTO> listarAsistentesParaOrganizador(Long eventoId, Long organizadorId, String busqueda) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new CustomException("No se encontró el evento con ID: " + eventoId, HttpStatus.NOT_FOUND));
        if (organizadorId == null || !organizadorId.equals(evento.getOrganizadorId())) {
            throw new CustomException("Solo el organizador del evento puede consultar sus asistentes.", HttpStatus.FORBIDDEN);
        }
        return obtenerAsistentes(evento, busqueda);
    }

    private List<AsistenteEventoDTO> obtenerAsistentes(Evento evento, String busqueda) {
        String filtro = busqueda == null ? null : busqueda.trim().toLowerCase(Locale.ROOT);
        List<Inscripcion> inscripciones = inscripcionRepository.findByEventoId(evento.getId());

        List<Long> usuarioIds = inscripciones.stream()
                .filter(ins -> ins.getEstado() != EstadoInscripcion.CANCELADO)
                .map(Inscripcion::getUsuarioId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Usuario> usuariosPorId = new HashMap<>();
        if (!usuarioIds.isEmpty()) {
            usuarioRepository.findAllById(usuarioIds)
                    .forEach(u -> usuariosPorId.put(u.getId(), u));
        }

        List<AsistenteEventoDTO> resultado = new ArrayList<>();
        for (Inscripcion ins : inscripciones) {
            if (ins.getEstado() == EstadoInscripcion.CANCELADO) {
                continue;
            }
            Usuario asistente = usuariosPorId.get(ins.getUsuarioId());
            if (asistente == null) {
                continue;
            }
            if (filtro != null && !filtro.isBlank() && !coincideFiltro(asistente, ins, filtro)) {
                continue;
            }
            resultado.add(toAsistenteDto(ins, asistente));
        }
        return resultado;
    }

    // Pantalla del organizador: inscritos vs gente dentro (ASISTIO sin check-out).
    @Override
    @Transactional(readOnly = true)
    public AforoEnVivoDTO consultarAforoEnVivo(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new CustomException("No se encontró el evento con ID: " + eventoId, HttpStatus.NOT_FOUND));
        long inscritosActivos = inscripcionRepository.countByEventoIdAndEstado(eventoId, EstadoInscripcion.INSCRITO);
        long asistencias = inscripcionRepository.countByEventoIdAndEstado(eventoId, EstadoInscripcion.ASISTIO);
        long presentes = inscripcionRepository.countByEventoIdAndEstadoAndFechaCheckOutIsNull(eventoId, EstadoInscripcion.ASISTIO);
        long inscritosTotales = inscritosActivos + asistencias;
        long disponibles = Math.max(0L, evento.getAforoMaximo() - inscritosTotales);
        double porcentaje = evento.getAforoMaximo() == 0
                ? 0.0
                : ((double) presentes / evento.getAforoMaximo()) * 100.0;

        AforoEnVivoDTO dto = new AforoEnVivoDTO();
        dto.setEventoId(evento.getId());
        dto.setNombreEvento(evento.getNombre());
        dto.setAforoMaximo(evento.getAforoMaximo());
        dto.setInscritos(inscritosTotales);
        dto.setAsistencias(asistencias);
        dto.setPresentes(presentes);
        dto.setCuposDisponibles(disponibles);
        dto.setPorcentajeOcupacion(Math.round(porcentaje * 100.0) / 100.0);
        return dto;
    }

    @Override
    public ResultadoCargaAsistentesDTO cargarAsistentesManual(Long eventoId, Long organizadorId, List<FilaAsistenteCargaDTO> filas) {
        // Por fila: creo usuario si no existe (pass = documento), inscribo y acumulo errores sin frenar todo el lote.
        if (filas == null || filas.isEmpty()) {
            throw new CustomException("Debe enviar al menos una fila de asistentes.", HttpStatus.BAD_REQUEST);
        }
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new CustomException("El evento no existe.", HttpStatus.NOT_FOUND));
        if (!organizadorId.equals(evento.getOrganizadorId())) {
            throw new CustomException("Solo el organizador del evento puede cargar asistentes.", HttpStatus.FORBIDDEN);
        }
        if (evento.getEstado() != EstadoEvento.ACTIVO) {
            throw new CustomException("Solo se pueden cargar asistentes cuando el evento está ACTIVO.", HttpStatus.BAD_REQUEST);
        }

        ResultadoCargaAsistentesDTO res = new ResultadoCargaAsistentesDTO();
        Set<String> emailsLote = new HashSet<>();
        int linea = 0;
        for (FilaAsistenteCargaDTO fila : filas) {
            linea++;
            try {
                procesarFila(evento, fila, res, emailsLote);
            } catch (Exception e) {
                res.getErrores().add("Línea " + linea + ": " + e.getMessage());
            }
        }
        notificacionService.crear(organizadorId,
                String.format("Carga de asistentes finalizada para \"%s\". Cuentas nuevas: %d. Inscripciones registradas: %d. Omitidos: %d. Errores: %d.",
                        evento.getNombre(),
                        res.getCuentasNuevasCreadas(),
                        res.getInscripcionesRegistradas(),
                        res.getFilasOmitidasDuplicadoUOtros(),
                        res.getErrores().size()),
                TipoNotificacion.INFO);
        return res;
    }

    @Override
    public ResultadoCargaAsistentesDTO cargarAsistentesCsv(Long eventoId, Long organizadorId, String contenidoCsv) {
        return cargarAsistentesManual(eventoId, organizadorId, asistenteCsvParser.parseCsv(contenidoCsv));
    }

    @Override
    public void asignarStaff(Long eventoId, Long organizadorId, Long staffUsuarioId, FuncionStaff funcion) {
        staffEventoService.asignarStaff(eventoId, organizadorId, staffUsuarioId, funcion);
    }

    @Override
    public StaffAsignadoDTO cambiarFuncionStaff(Long eventoId, Long organizadorId, Long staffUsuarioId, FuncionStaff funcion) {
        return staffEventoService.cambiarFuncionStaff(eventoId, organizadorId, staffUsuarioId, funcion);
    }

    @Override
    public void desasignarStaff(Long eventoId, Long organizadorId, Long staffUsuarioId) {
        staffEventoService.desasignarStaff(eventoId, organizadorId, staffUsuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listarStaffIdsPorEvento(Long eventoId) {
        return staffEventoService.listarStaffIdsPorEvento(eventoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffAsignadoDTO> listarStaffPorEvento(Long eventoId) {
        return staffEventoService.listarStaffPorEvento(eventoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoStaffDTO> listarEventosDelStaff(Long staffUsuarioId) {
        return staffEventoService.listarEventosDelStaff(staffUsuarioId);
    }

    private void procesarFila(Evento evento, FilaAsistenteCargaDTO fila,
                              ResultadoCargaAsistentesDTO res, Set<String> emailsLote) {
        // emailsLote evita duplicados en el mismo CSV en una sola subida.
        validarCamposMinimos(fila);
        String emailNorm = fila.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!emailNorm.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Correo con formato inválido.");
        }
        if (!emailsLote.add(emailNorm)) {
            res.setFilasOmitidasDuplicadoUOtros(res.getFilasOmitidasDuplicadoUOtros() + 1);
            return;
        }
        String numeroDoc = fila.getNumeroDocumento().trim();
        Optional<Usuario> existente = usuarioRepository.findByEmail(emailNorm);

        if (existente.isPresent()) {
            Usuario u = existente.get();
            if (u.getEstado() != Estado.ACTIVO) {
                throw new IllegalStateException("El correo pertenece a una cuenta no ACTIVA (estado: " + u.getEstado() + ").");
            }
            Optional<Inscripcion> insOpt = inscripcionRepository.findByUsuarioIdAndEventoId(u.getId(), evento.getId());
            if (insOpt.isPresent() && insOpt.get().getEstado() != EstadoInscripcion.CANCELADO) {
                res.setFilasOmitidasDuplicadoUOtros(res.getFilasOmitidasDuplicadoUOtros() + 1);
                return;
            }
            inscribirEnCarga(res, u.getId(), evento, false);
            return;
        }

        // Cuenta nueva: contraseña inicial = número de documento (así entran sin correo de bienvenida externo).
        Usuario nuevo = new Usuario();
        nuevo.setNombre(fila.getNombre().trim());
        nuevo.setEmail(emailNorm);
        nuevo.setTelefono(blankToNull(fila.getTelefono()));
        nuevo.setTipoDocumento(fila.getTipoDocumento().trim());
        nuevo.setNumeroDocumento(numeroDoc);
        nuevo.setPassword(passwordEncoder.encode(numeroDoc));
        nuevo.setRol(Rol.ASISTENTE);
        nuevo.setEstado(Estado.ACTIVO);
        Usuario guardado = usuarioRepository.save(nuevo);
        res.setCuentasNuevasCreadas(res.getCuentasNuevasCreadas() + 1);

        notificacionService.crear(guardado.getId(),
                String.format("Tu cuenta fue creada para el evento \"%s\". Contraseña inicial: tu número de documento registrado.", evento.getNombre()),
                TipoNotificacion.INFO);
        inscribirEnCarga(res, guardado.getId(), evento, true);
    }

    private void inscribirEnCarga(ResultadoCargaAsistentesDTO res, Long usuarioId, Evento evento, boolean yaNotificadoAlta) {
        try {
            inscribirInterno(usuarioId, evento.getId(), false);
            res.setInscripcionesRegistradas(res.getInscripcionesRegistradas() + 1);
            if (!yaNotificadoAlta) {
                notificacionService.crear(usuarioId,
                        String.format("Quedaste inscrito en el evento \"%s\". Usa tu correo para iniciar sesión.", evento.getNombre()),
                        TipoNotificacion.INFO);
            }
        } catch (CustomException e) {
            if (e.getStatus() == HttpStatus.CONFLICT) {
                res.setFilasOmitidasDuplicadoUOtros(res.getFilasOmitidasDuplicadoUOtros() + 1);
            } else {
                throw new IllegalStateException(e.getMessage());
            }
        }
    }

    private void validarCamposMinimos(FilaAsistenteCargaDTO fila) {
        if (fila.getNombre() == null || fila.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (fila.getEmail() == null || fila.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio.");
        }
        if (fila.getTipoDocumento() == null || fila.getTipoDocumento().isBlank()) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }
        if (fila.getNumeroDocumento() == null || fila.getNumeroDocumento().isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio (se usa como contraseña inicial).");
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    // Búsqueda del staff: cada palabra del filtro debe aparecer en nombre, email, doc o QR.
    private static boolean coincideFiltro(Usuario u, Inscripcion ins, String filtro) {
        String hay = construirTextoBusquedaAsistente(u, ins);
        if (hay.isEmpty()) {
            return false;
        }
        for (String token : filtro.split("\\s+")) {
            if (token.isBlank()) {
                continue;
            }
            if (!hay.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private static String construirTextoBusquedaAsistente(Usuario u, Inscripcion ins) {
        StringBuilder sb = new StringBuilder();
        appendNorm(sb, u.getNombre());
        appendNorm(sb, u.getEmail());
        appendNorm(sb, u.getTelefono());
        appendNorm(sb, u.getTipoDocumento());
        appendNorm(sb, u.getNumeroDocumento());
        if (ins.getCodigoQR() != null && !ins.getCodigoQR().isBlank()) {
            appendNorm(sb, ins.getCodigoQR());
        }
        return sb.toString();
    }

    private static void appendNorm(StringBuilder sb, String part) {
        if (part == null || part.isBlank()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(part.toLowerCase(Locale.ROOT).trim());
    }

    private Inscripcion buscarInscripcion(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Inscripción no encontrada con ID: " + id, HttpStatus.NOT_FOUND));
    }

    private InscripcionDTO toDto(Inscripcion ins) {
        return modelMapper.map(ins, InscripcionDTO.class);
    }

    private void enriquecerDatosCheckInEnDto(InscripcionDTO dto, Long usuarioId, Long eventoId) {
        usuarioRepository.findById(usuarioId).ifPresent(u -> {
            dto.setNombreAsistente(u.getNombre());
            dto.setEmailAsistente(u.getEmail());
            dto.setTipoDocumento(u.getTipoDocumento());
            dto.setNumeroDocumento(u.getNumeroDocumento());
        });
        eventoRepository.findById(eventoId).ifPresent(ev -> {
            dto.setNombreEvento(ev.getNombre());
            dto.setFechaEvento(ev.getFecha());
            dto.setFechaFinEvento(ev.getFechaFin());
            dto.setUbicacionEvento(ev.getUbicacion());
        });
    }

    private AsistenteEventoDTO toAsistenteDto(Inscripcion ins, Usuario u) {
        AsistenteEventoDTO dto = new AsistenteEventoDTO();
        dto.setInscripcionId(ins.getId());
        dto.setUsuarioId(u.getId());
        dto.setNombre(u.getNombre());
        dto.setEmail(u.getEmail());
        dto.setTelefono(u.getTelefono());
        dto.setTipoDocumento(u.getTipoDocumento());
        dto.setNumeroDocumento(u.getNumeroDocumento());
        dto.setEstadoInscripcion(ins.getEstado());
        dto.setFechaInscripcion(ins.getFechaInscripcion());
        dto.setFechaCheckIn(ins.getFechaCheckIn());
        dto.setFechaCheckOut(ins.getFechaCheckOut());
        dto.setCodigoQR(ins.getCodigoQR());
        return dto;
    }
}
