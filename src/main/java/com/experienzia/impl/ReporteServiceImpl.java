package com.experienzia.impl;

import com.experienzia.dto.AsistenciaDTO;
import com.experienzia.dto.AsistenteEventoDTO;
import com.experienzia.dto.CurvaIngresoPuntoDTO;
import com.experienzia.dto.DashboardAdminDTO;
import com.experienzia.dto.DashboardOrganizadorDTO;
import com.experienzia.dto.DesempenoStaffDTO;
import com.experienzia.dto.EventoPopularDTO;
import com.experienzia.dto.PuntoSerieDTO;
import com.experienzia.dto.PagoReporteLineaDTO;
import com.experienzia.dto.ReporteEventoAvanzadoDTO;
import com.experienzia.dto.ReporteEventoDTO;
import com.experienzia.dto.ReportePagosAdminDTO;
import com.experienzia.dto.ReporteUsuariosAdminDTO;
import com.experienzia.dto.ResumenDTO;
import com.experienzia.entity.Estado;
import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.EstadoInscripcion;
import com.experienzia.entity.EstadoPago;
import com.experienzia.entity.Evento;
import com.experienzia.entity.Pago;
import com.experienzia.entity.FuncionStaff;
import com.experienzia.entity.Inscripcion;
import com.experienzia.entity.Rol;
import com.experienzia.entity.StaffEventoAsignacion;
import com.experienzia.entity.Usuario;
import com.experienzia.exceptions.CustomException;
import com.experienzia.repository.AuditoriaRepository;
import com.experienzia.repository.EventoRepository;
import com.experienzia.repository.InscripcionRepository;
import com.experienzia.repository.PagoRepository;
import com.experienzia.repository.StaffEventoAsignacionRepository;
import com.experienzia.repository.UsuarioRepository;
import com.experienzia.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
// Reportes y dashboards: trato de no hacer findAll de todo porque se muere la BD con muchos datos.
public class ReporteServiceImpl implements ReporteService {

    private static final String ENTIDAD_INSCRIPCION = "Inscripcion";

    // Acciones de auditoría que uso para contar check-in QR vs manual en el reporte avanzado.
    private static final List<String> ACCIONES_CHECK_IN_QR = List.of("CHECK_IN_QR");
    private static final List<String> ACCIONES_CHECK_IN_MANUAL = List.of("CHECK_IN");
    private static final List<String> ACCIONES_CHECK_OUT = List.of("CHECK_OUT", "CHECK_OUT_QR");

    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final StaffEventoAsignacionRepository staffEventoRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final PagoRepository pagoRepository;

    public ReporteServiceImpl(
            InscripcionRepository inscripcionRepository,
            EventoRepository eventoRepository,
            UsuarioRepository usuarioRepository,
            StaffEventoAsignacionRepository staffEventoRepository,
            AuditoriaRepository auditoriaRepository,
            PagoRepository pagoRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.staffEventoRepository = staffEventoRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.pagoRepository = pagoRepository;
    }

    @Override
    public List<EventoPopularDTO> obtenerEventosPopulares() {
        // Query agrupada en repo; acá solo le pongo el nombre del evento para el ranking.
        return inscripcionRepository.findEventosPopulares().stream().map(obj -> {
            Long eventoId = (Long) obj[0];
            Long totalInscritos = (Long) obj[1];
            String nombre = eventoRepository.findById(eventoId)
                    .map(Evento::getNombre)
                    .orElse("Evento Desconocido");
            return new EventoPopularDTO(eventoId, nombre, totalInscritos);
        }).toList();
    }

    @Override
    public AsistenciaDTO obtenerAsistenciaPorEvento(Long eventoId) {
        long totalAsistieron = inscripcionRepository.countByEventoIdAndEstado(eventoId, EstadoInscripcion.ASISTIO);
        return new AsistenciaDTO(eventoId, totalAsistieron);
    }

    @Override
    public List<Long> obtenerUsuariosPorEvento(Long eventoId) {
        return inscripcionRepository.findUsuarioIdsByEventoId(eventoId);
    }

    @Override
    public ResumenDTO obtenerResumenGeneral() {
        return new ResumenDTO(
                usuarioRepository.count(),
                eventoRepository.count(),
                inscripcionRepository.count());
    }

    @Override
    public ReporteEventoDTO obtenerReporteDetalladoEvento(Long eventoId, Long organizadorId) {
        Evento evento = buscarEventoYValidarOrganizador(eventoId, organizadorId);

        // Recorro inscripciones y armo lista de asistentes; cargo usuarios en batch para no hacer N+1.
        List<Inscripcion> inscripciones = inscripcionRepository.findByEventoId(eventoId);
        long inscritos = 0L;
        long asistencias = 0L;
        long enSala = 0L;
        List<AsistenteEventoDTO> asistentes = new ArrayList<>();

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

        for (Inscripcion ins : inscripciones) {
            if (ins.getEstado() == EstadoInscripcion.CANCELADO) {
                continue;
            }
            inscritos++;
            if (ins.getEstado() == EstadoInscripcion.ASISTIO) {
                asistencias++;
                // Sin check-out = todavía dentro del salón (aforo en vivo del reporte).
                if (ins.getFechaCheckOut() == null) {
                    enSala++;
                }
            }
            Usuario u = usuariosPorId.get(ins.getUsuarioId());
            if (u != null) {
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
                asistentes.add(dto);
            }
        }

        if (inscritos == 0 && asistencias == 0) {
            throw new CustomException("No hay datos suficientes para generar el reporte de este evento.",
                    HttpStatus.NOT_FOUND);
        }

        double porcentajeOcupacion = evento.getAforoMaximo() == 0
                ? 0.0
                : ((double) asistencias / evento.getAforoMaximo()) * 100.0;
        double porcentajeAsistencia = inscritos == 0
                ? 0.0
                : ((double) asistencias / inscritos) * 100.0;

        ReporteEventoDTO r = new ReporteEventoDTO();
        r.setEventoId(evento.getId());
        r.setNombreEvento(evento.getNombre());
        r.setEstadoEvento(evento.getEstado() != null ? evento.getEstado().name() : null);
        r.setFechaEvento(evento.getFecha());
        r.setDuracionHoras(evento.getDuracionHoras());
        r.setAforoMaximo(evento.getAforoMaximo());
        r.setInscritos(inscritos);
        r.setAsistenciasReales(asistencias);
        r.setAsistentesActualmenteEnSala(enSala);
        r.setPorcentajeOcupacion(redondear(porcentajeOcupacion));
        r.setPorcentajeAsistenciaSobreInscritos(redondear(porcentajeAsistencia));
        r.setAsistentes(asistentes);
        return r;
    }

    @Override
    // Curva por hora, QR vs manual y desempeño del staff — me apoyo en auditoría.
    public ReporteEventoAvanzadoDTO obtenerReporteAvanzadoEvento(Long eventoId, Long organizadorId) {
        Evento evento = buscarEventoYValidarOrganizador(eventoId, organizadorId);

        List<Inscripcion> inscripciones = inscripcionRepository.findByEventoId(eventoId);
        long inscritos = 0L;
        long asistieron = 0L;
        // Array [ingresos, salidas] por hora del día — para la curva que muestra el front.
        // Array [ingresos, salidas] por hora del día — el front arma la gráfica.
        Map<Integer, long[]> porHora = new LinkedHashMap<>();
        for (int h = 0; h < 24; h++) {
            porHora.put(h, new long[]{0L, 0L});
        }

        long checkOuts = 0L;
        List<Long> inscripcionIds = new ArrayList<>();
        for (Inscripcion ins : inscripciones) {
            inscripcionIds.add(ins.getId());
            if (ins.getEstado() == EstadoInscripcion.CANCELADO) {
                continue;
            }
            inscritos++;
            if (ins.getFechaCheckIn() != null) {
                asistieron++;
                int h = ins.getFechaCheckIn().getHour();
                porHora.get(h)[0]++;
            }
            if (ins.getFechaCheckOut() != null) {
                checkOuts++;
                int h = ins.getFechaCheckOut().getHour();
                porHora.get(h)[1]++;
            }
        }

        // Cuántos check-in fueron por QR vs manual lo saco de auditoría, no solo de la inscripción.
        long checkInsQR = contarAuditoriasInscripciones(inscripcionIds, ACCIONES_CHECK_IN_QR);
        long checkInsManuales = contarAuditoriasInscripciones(inscripcionIds, ACCIONES_CHECK_IN_MANUAL);

        List<StaffEventoAsignacion> asignaciones = staffEventoRepository.findByEventoId(eventoId);
        List<Long> staffIds = asignaciones.stream()
                .map(StaffEventoAsignacion::getStaffUsuarioId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Usuario> staffPorId = new HashMap<>();
        if (!staffIds.isEmpty()) {
            usuarioRepository.findAllById(staffIds)
                    .forEach(u -> staffPorId.put(u.getId(), u));
        }

        List<DesempenoStaffDTO> desempeno = new ArrayList<>();
        for (StaffEventoAsignacion a : asignaciones) {
            Usuario staff = staffPorId.get(a.getStaffUsuarioId());
            DesempenoStaffDTO d = new DesempenoStaffDTO();
            d.setStaffUsuarioId(a.getStaffUsuarioId());
            d.setNombre(staff != null ? staff.getNombre() : null);
            d.setFuncion(a.getFuncion() == null ? FuncionStaff.GENERAL.name() : a.getFuncion().name());

            long sInQr = contarAuditoriasStaffEnEvento(a.getStaffUsuarioId(), inscripcionIds, ACCIONES_CHECK_IN_QR);
            long sInMan = contarAuditoriasStaffEnEvento(a.getStaffUsuarioId(), inscripcionIds, ACCIONES_CHECK_IN_MANUAL);
            long sOut = contarAuditoriasStaffEnEvento(a.getStaffUsuarioId(), inscripcionIds, ACCIONES_CHECK_OUT);
            d.setCheckInsRegistrados(sInQr + sInMan);
            d.setCheckOutsRegistrados(sOut);
            d.setCheckInsPorQR(sInQr);
            d.setCheckInsManuales(sInMan);
            desempeno.add(d);
        }

        List<CurvaIngresoPuntoDTO> curva = new ArrayList<>();
        for (var entry : porHora.entrySet()) {
            CurvaIngresoPuntoDTO p = new CurvaIngresoPuntoDTO();
            p.setHora(entry.getKey());
            p.setIngresos(entry.getValue()[0]);
            p.setSalidas(entry.getValue()[1]);
            curva.add(p);
        }

        long faltaron = Math.max(0L, inscritos - asistieron);
        double porcentajeOcupacion = evento.getAforoMaximo() == 0
                ? 0.0 : ((double) asistieron / evento.getAforoMaximo()) * 100.0;
        double porcentajeAsistencia = inscritos == 0
                ? 0.0 : ((double) asistieron / inscritos) * 100.0;

        ReporteEventoAvanzadoDTO r = new ReporteEventoAvanzadoDTO();
        r.setEventoId(evento.getId());
        r.setNombreEvento(evento.getNombre());
        r.setFechaEvento(evento.getFecha());
        r.setAforoMaximo(evento.getAforoMaximo());
        r.setInscritos(inscritos);
        r.setAsistieron(asistieron);
        r.setFaltaron(faltaron);
        r.setPorcentajeOcupacion(redondear(porcentajeOcupacion));
        r.setPorcentajeAsistencia(redondear(porcentajeAsistencia));
        r.setCheckInsTotal(asistieron);
        r.setCheckInsPorQR(checkInsQR);
        r.setCheckInsManuales(checkInsManuales);
        r.setCheckOutsTotal(checkOuts);
        r.setCurvaIngreso(curva);
        r.setDesempenoStaff(desempeno);
        return r;
    }

    @Override
    // KPIs del organizador + series de últimos 12 meses.
    public DashboardOrganizadorDTO obtenerDashboardOrganizador(Long organizadorId) {
        // Panel del organizador: conteos por estado + series de últimos 12 meses para gráficas.
        if (organizadorId == null) {
            throw new CustomException("organizadorId es obligatorio.", HttpStatus.BAD_REQUEST);
        }
        Usuario org = usuarioRepository.findById(organizadorId)
                .orElseThrow(() -> new CustomException("Organizador no encontrado.", HttpStatus.NOT_FOUND));
        if (org.getRol() != Rol.ORGANIZADOR) {
            throw new CustomException("El usuario no es ORGANIZADOR.", HttpStatus.FORBIDDEN);
        }

        long activos = eventoRepository.countByOrganizadorIdAndEstado(organizadorId, EstadoEvento.ACTIVO);
        long pendientes = eventoRepository.countByOrganizadorIdAndEstado(organizadorId, EstadoEvento.PENDIENTE);
        long cancelados = eventoRepository.countByOrganizadorIdAndEstado(organizadorId, EstadoEvento.CANCELADO);
        long eventosTotales = eventoRepository.countByOrganizadorId(organizadorId);
        long cuposActivos = eventoRepository.sumAforoActualEventosActivosByOrganizadorId(organizadorId);

        LocalDateTime desdeSerie = inicioVentanaSerieMensual();
        Map<YearMonth, Long> inscripcionesPorMes = nuevaSerieMensualVacia();
        aplicarConteosMensuales(
                inscripcionesPorMes,
                inscripcionRepository.countInscripcionesActivasAgrupadasPorMesByOrganizadorId(organizadorId, desdeSerie));

        Map<YearMonth, Long> eventosPorMes = nuevaSerieMensualVacia();
        aplicarConteosMensuales(
                eventosPorMes,
                eventoRepository.countEventosAgrupadosPorMesByOrganizadorId(organizadorId, desdeSerie));

        long totalInscritos = inscripcionRepository.countInscripcionesActivasByOrganizadorId(organizadorId);
        long asistencias30 = inscripcionRepository.countAsistenciasConCheckInDesdeByOrganizadorId(
                organizadorId, LocalDateTime.now().minusDays(30));

        DashboardOrganizadorDTO dto = new DashboardOrganizadorDTO();
        dto.setOrganizadorId(organizadorId);
        dto.setEventosActivos(activos);
        dto.setEventosPendientes(pendientes);
        dto.setEventosCancelados(cancelados);
        dto.setEventosTotales(eventosTotales);
        dto.setTotalInscritos(totalInscritos);
        dto.setAforoMaximoPorEvento(600); // tope de negocio fijo para la UI del dashboard
        dto.setCuposOcupadosEventosActivos(cuposActivos);
        dto.setAsistenciasUltimos30Dias(asistencias30);
        dto.setSerieMensualEventos(toSerie(eventosPorMes));
        dto.setSerieMensualInscripciones(toSerie(inscripcionesPorMes));
        return dto;
    }

    @Override
    public DashboardAdminDTO obtenerDashboardAdmin() {
        long activos = eventoRepository.countByEstado(EstadoEvento.ACTIVO);
        long pendientes = eventoRepository.countByEstado(EstadoEvento.PENDIENTE);
        long cancelados = eventoRepository.countByEstado(EstadoEvento.CANCELADO);
        long eventosTotales = eventoRepository.count();

        LocalDateTime desdeSerie = inicioVentanaSerieMensual();
        Map<YearMonth, Long> eventosPorMes = nuevaSerieMensualVacia();
        aplicarConteosMensuales(eventosPorMes, eventoRepository.countEventosAgrupadosPorMesDesde(desdeSerie));

        long usuariosActivos = usuarioRepository.countUsuariosByEstado(Estado.ACTIVO);
        long usuariosPendientes = usuarioRepository.countUsuariosByEstado(Estado.PENDIENTE);
        long organizadoresActivos = usuarioRepository.countUsuariosByRolAndEstado(Rol.ORGANIZADOR, Estado.ACTIVO);
        long asistentes = usuarioRepository.countUsuariosByRol(Rol.ASISTENTE);
        long staff = usuarioRepository.countUsuariosByRol(Rol.STAFF);
        long usuariosTotales = usuarioRepository.count();

        Map<YearMonth, Long> usuariosPorMes = nuevaSerieMensualVacia();
        // No tenemos fecha de alta de usuario en BD; meto el total en el mes actual como parche para la gráfica.
        usuariosPorMes.computeIfPresent(YearMonth.now(), (k, v) -> v + usuariosTotales);

        DashboardAdminDTO dto = new DashboardAdminDTO();
        dto.setEventosActivos(activos);
        dto.setEventosPendientes(pendientes);
        dto.setEventosCancelados(cancelados);
        dto.setEventosTotales(eventosTotales);
        dto.setUsuariosTotales(usuariosTotales);
        dto.setUsuariosActivos(usuariosActivos);
        dto.setUsuariosPendientes(usuariosPendientes);
        dto.setOrganizadoresActivos(organizadoresActivos);
        dto.setAsistentesTotales(asistentes);
        dto.setStaffTotales(staff);
        dto.setInscripcionesTotales(inscripcionRepository.count());
        dto.setSerieMensualEventos(toSerie(eventosPorMes));
        dto.setSerieMensualUsuarios(toSerie(usuariosPorMes));
        return dto;
    }

    @Override
    public ReportePagosAdminDTO obtenerReportePagosAdmin() {
        double totalAprobado = pagoRepository.sumMontoByEstado(EstadoPago.APROBADO);
        double complementos = pagoRepository.sumMontoComplementosByEstado(EstadoPago.APROBADO);
        double pendiente = pagoRepository.sumMontoByEstado(EstadoPago.PENDIENTE);

        List<PagoReporteLineaDTO> lineas = new ArrayList<>();
        for (Pago p : pagoRepository.findAllByOrderByFechaDesc()) {
            PagoReporteLineaDTO linea = new PagoReporteLineaDTO();
            linea.setPagoId(p.getId());
            linea.setEventoId(p.getEventoId());
            linea.setOrganizadorId(p.getOrganizadorId());
            linea.setMonto(p.getMonto());
            linea.setEstado(p.getEstado());
            linea.setFecha(p.getFecha());
            linea.setComplementoHoras(p.getSaldoAprobadoPrevio() != null);
            eventoRepository.findById(p.getEventoId()).ifPresent(ev -> linea.setNombreEvento(ev.getNombre()));
            usuarioRepository.findById(p.getOrganizadorId()).ifPresent(u -> linea.setNombreOrganizador(u.getNombre()));
            lineas.add(linea);
        }

        return new ReportePagosAdminDTO(
                redondear(totalAprobado),
                redondear(complementos),
                redondear(pendiente),
                pagoRepository.countByEstado(EstadoPago.APROBADO),
                pagoRepository.countByEstado(EstadoPago.PENDIENTE),
                pagoRepository.countByEstado(EstadoPago.RECHAZADO),
                lineas);
    }

    @Override
    public ReporteUsuariosAdminDTO obtenerReporteUsuariosAdmin() {
        DashboardAdminDTO dash = obtenerDashboardAdmin();
        ReporteUsuariosAdminDTO dto = new ReporteUsuariosAdminDTO();
        dto.setUsuariosTotales(dash.getUsuariosTotales());
        dto.setUsuariosActivos(dash.getUsuariosActivos());
        dto.setUsuariosPendientes(dash.getUsuariosPendientes());
        dto.setOrganizadoresActivos(dash.getOrganizadoresActivos());
        dto.setAsistentesRegistrados(dash.getAsistentesTotales());
        dto.setStaffActivo(usuarioRepository.countUsuariosByRolAndEstado(Rol.STAFF, Estado.ACTIVO));
        dto.setCrecimientoMensualUsuarios(dash.getSerieMensualUsuarios());
        dto.setCrecimientoMensualEventos(dash.getSerieMensualEventos());
        return dto;
    }

    private long contarAuditoriasInscripciones(List<Long> inscripcionIds, List<String> acciones) {
        if (inscripcionIds.isEmpty()) {
            return 0L;
        }
        return auditoriaRepository.countByEntidadAndEntidadIdInAndAccionIn(
                ENTIDAD_INSCRIPCION, inscripcionIds, acciones);
    }

    private long contarAuditoriasStaffEnEvento(Long staffUsuarioId, List<Long> inscripcionIds, List<String> acciones) {
        if (inscripcionIds.isEmpty()) {
            return 0L;
        }
        return auditoriaRepository.countByUsuarioIdAndEntidadAndEntidadIdInAndAccionIn(
                staffUsuarioId, ENTIDAD_INSCRIPCION, inscripcionIds, acciones);
    }

    private static LocalDateTime inicioVentanaSerieMensual() {
        YearMonth inicio = YearMonth.now().minusMonths(11);
        return inicio.atDay(1).atStartOfDay();
    }

    private static void aplicarConteosMensuales(Map<YearMonth, Long> serie, List<Object[]> filas) {
        for (Object[] row : filas) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            YearMonth ym = YearMonth.of(year, month);
            serie.computeIfPresent(ym, (k, v) -> v + count);
        }
    }

    private Evento buscarEventoYValidarOrganizador(Long eventoId, Long organizadorId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new CustomException("Evento no encontrado.", HttpStatus.NOT_FOUND));
        if (organizadorId != null && evento.getOrganizadorId() != null
                && !evento.getOrganizadorId().equals(organizadorId)) {
            throw new CustomException("Solo el organizador del evento puede ver este reporte.", HttpStatus.FORBIDDEN);
        }
        return evento;
    }

    // Últimos 12 meses en cero para que el front siempre tenga 12 puntos aunque no haya datos.
    private static Map<YearMonth, Long> nuevaSerieMensualVacia() {
        Map<YearMonth, Long> map = new LinkedHashMap<>();
        YearMonth ahora = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            map.put(ahora.minusMonths(i), 0L);
        }
        return map;
    }

    private static List<PuntoSerieDTO> toSerie(Map<YearMonth, Long> map) {
        List<PuntoSerieDTO> serie = new ArrayList<>();
        for (var e : map.entrySet()) {
            serie.add(new PuntoSerieDTO(e.getKey().toString(), e.getValue()));
        }
        return serie;
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
