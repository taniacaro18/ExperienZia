package com.experienzia.impl;

import com.experienzia.dto.DisponibilidadSalonDTO;
import com.experienzia.dto.EventoDTO;
import com.experienzia.dto.EventoNovedadDTO;
import com.experienzia.dto.FranjaOcupacionSalonDTO;
import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.EstadoInscripcion;
import com.experienzia.entity.EstadoNovedadEvento;
import com.experienzia.entity.EstadoPago;
import com.experienzia.entity.Evento;
import com.experienzia.entity.Inscripcion;
import com.experienzia.entity.Pago;
import com.experienzia.entity.TipoEvento;
import com.experienzia.entity.TipoNotificacion;
import com.experienzia.exceptions.CustomException;
import com.experienzia.repository.EventoNovedadRepository;
import com.experienzia.repository.EventoRepository;
import com.experienzia.repository.InscripcionRepository;
import com.experienzia.repository.PagoRepository;
import com.experienzia.repository.UsuarioRepository;
import com.experienzia.service.EventoService;
import com.experienzia.service.InscripcionService;
import com.experienzia.service.NotificacionService;
import com.experienzia.spec.EventoSpecification;
import com.experienzia.spec.EventoSpecification.EventoSearchCriteria;
import com.experienzia.util.EventoVentanaUtil;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
// Esta clase es como el "cerebro" de eventos: acá llegan las peticiones del controller y yo reparto el trabajo a los helpers
public class EventoServiceImpl implements EventoService {

    // Inyecto de todo un poco porque si meto todo acá el archivo queda interminable (ya de por sí está larguísimo)
    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final PagoRepository pagoRepository;
    private final EventoNovedadRepository eventoNovedadRepository;
    private final NotificacionService notificacionService;
    private final InscripcionService inscripcionService;
    private final ModelMapper modelMapper;
    private final UsuarioRepository usuarioRepository;
    private final EventoValidator validator;
    private final EventoFinanzasHelper finanzas;
    private final EventoNovedadService novedad;
    private final EventoRelojService reloj;
    private final EventoMapeadorHelper mapeador;

    public EventoServiceImpl(
            EventoRepository eventoRepository,
            InscripcionRepository inscripcionRepository,
            PagoRepository pagoRepository,
            EventoNovedadRepository eventoNovedadRepository,
            NotificacionService notificacionService,
            InscripcionService inscripcionService,
            ModelMapper modelMapper,
            UsuarioRepository usuarioRepository,
            EventoValidator validator,
            EventoFinanzasHelper finanzas,
            EventoNovedadService novedad,
            EventoRelojService reloj,
            EventoMapeadorHelper mapeador) {
        this.eventoRepository = eventoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.pagoRepository = pagoRepository;
        this.eventoNovedadRepository = eventoNovedadRepository;
        this.notificacionService = notificacionService;
        this.inscripcionService = inscripcionService;
        this.modelMapper = modelMapper;
        this.usuarioRepository = usuarioRepository;
        this.validator = validator;
        this.finanzas = finanzas;
        this.novedad = novedad;
        this.reloj = reloj;
        this.mapeador = mapeador;
    }

    @Override
    public EventoDTO crear(EventoDTO dto) {
        // Si el organizador no manda el ID, freno acá para que el front no se estalle con un error raro de la BD
        if (dto.getOrganizadorId() == null) {
            throw new CustomException("El organizador es requerido.", HttpStatus.BAD_REQUEST);
        }
        validator.validarAforo(dto.getAforoMaximo());
        if (dto.getTipoEvento() == null) {
            throw new CustomException("El tipo de evento es requerido (PUBLICO/PRIVADO).", HttpStatus.BAD_REQUEST);
        }
        LocalDateTime inicio = dto.getFecha();
        // Esto de cruzar medianoche me confundió un montón; el validator arregla la fecha fin si el evento termina al día siguiente
        LocalDateTime finAjustado = validator.ajustarFinCruceMedianoche(inicio, dto.getFechaFin());
        validator.validarFechas(inicio, finAjustado);
        // Antes de guardar reviso que el salón/ubicación no choque con otro evento ya programado
        validator.assertUbicacionDisponible(inicio, finAjustado, dto.getUbicacion(), null);

        Evento evento = modelMapper.map(dto, Evento.class);
        evento.setId(null);
        evento.setAforoMaximo(dto.getAforoMaximo());
        evento.setUbicacion(validator.ubicacionFinal(dto.getUbicacion()));
        evento.setFechaFin(finAjustado);
        evento.setDuracionHoras(reloj.calcularDuracionHoras(inicio, finAjustado));
        evento.setCosto(finanzas.calcularCosto(evento.getDuracionHoras()));
        // Todo evento nuevo arranca PENDIENTE hasta que el admin lo apruebe (o pague si aplica)
        evento.setEstado(EstadoEvento.PENDIENTE);
        evento.setAforoActual(0);
        evento.setMotivoRechazo(null);
        evento.setMotivoCancelacion(null);
        evento.setEstadoPrevioRevision(null);
        Evento guardado = eventoRepository.save(evento);
        notificacionService.notificarAdministradores(
                "Nueva solicitud de evento «" + guardado.getNombre() + "» ("
                        + guardado.getTipoEvento() + ") pendiente de aprobación.",
                TipoNotificacion.INFO);
        return mapeador.toDto(guardado);
    }

    @Override
    public EventoDTO editar(Long id, EventoDTO dto) {
        // Primero actualizo los que ya se acabaron por tiempo, para no editar algo que en realidad ya terminó
        reloj.marcarEventosActivosFinalizados();
        Evento evento = buscarPorId(id);
        if (evento.getEstado() == EstadoEvento.ACTIVO && reloj.eventoHaFinalizadoSuVentana(evento)) {
            evento.setEstado(EstadoEvento.FINALIZADO);
            eventoRepository.save(evento);
        }
        validarEdicionPermitida(evento, dto);

        LocalDateTime inicio = dto.getFecha();
        LocalDateTime finAjustado = validator.ajustarFinCruceMedianoche(inicio, dto.getFechaFin());
        validator.validarFechas(inicio, finAjustado);

        int viejaDuracion = resolverViejaDuracion(evento);
        EstadoEvento estadoAntes = evento.getEstado();
        int nuevaDuracion = reloj.calcularDuracionHoras(inicio, finAjustado);
        double costoFinal = finanzas.calcularCosto(nuevaDuracion);
        double viejoCosto = evento.getCosto();

        // El validator me dice qué cambió (fechas, ubicación, aforo...) para no validar de más
        CambiosEdicionEvento cambios = validator.evaluarCambiosEdicion(
                evento, dto, inicio, finAjustado, nuevaDuracion, viejaDuracion);
        if (cambios.requiereValidarUbicacion()) {
            validator.assertUbicacionDisponible(inicio, finAjustado, cambios.ubicNueva(), evento.getId());
        }

        // Guardo cómo estaba el evento por si el admin rechaza y hay que volver atrás
        Map<String, Object> snapshotAntes = novedad.snapshotEventoParaRevert(evento);
        validator.aplicarDatosEdicionAlEvento(evento, dto, inicio, finAjustado, cambios, nuevaDuracion, costoFinal);

        Optional<Pago> pOpt = pagoRepository.findByEventoId(evento.getId());
        boolean pagoAprobado = pOpt.isPresent() && pOpt.get().getEstado() == EstadoPago.APROBADO;

        if (costoFinal <= 0.0) {
            return editarEventoSinCosto(evento, estadoAntes);
        }

        EventoEdicionFlujoContext ctx = new EventoEdicionFlujoContext(
                evento, dto, estadoAntes, viejaDuracion, nuevaDuracion, viejoCosto, costoFinal,
                cambios, snapshotAntes, pOpt, pagoAprobado);

        // Esta cadena de .or() me costó entenderla: va probando flujos (más horas, menos horas, revisión admin...) hasta que uno cuadre
        return novedad.procesarAumentoHoras(ctx)
                .or(() -> finanzas.procesarDisminucionHoras(ctx))
                .or(() -> novedad.procesarRevisionMetadatos(ctx))
                .or(() -> finanzas.procesarCambiosDuracionConPago(ctx))
                .orElseGet(() -> finanzas.finalizarEdicionPendiente(ctx));
    }

    private void validarEdicionPermitida(Evento evento, EventoDTO dto) {
        // Solo el dueño del evento puede editarlo; si no, mando 403 y el front muestra "no autorizado"
        if (dto.getOrganizadorId() == null || !dto.getOrganizadorId().equals(evento.getOrganizadorId())) {
            throw new CustomException("Solo el organizador del evento puede editarlo.", HttpStatus.FORBIDDEN);
        }
        if (evento.getEstado() == EstadoEvento.CANCELADO) {
            throw new CustomException("No se puede editar un evento CANCELADO.", HttpStatus.BAD_REQUEST);
        }
        if (evento.getEstado() == EstadoEvento.FINALIZADO) {
            throw new CustomException("No se puede editar un evento FINALIZADO.", HttpStatus.BAD_REQUEST);
        }
        if (estadoBloqueaEdicion(evento.getEstado())) {
            throw new CustomException(
                    "Este evento tiene un trámite pendiente de administración o de pago adicional por horas. "
                            + "Espera la resolución antes de volver a editarlo.",
                    HttpStatus.CONFLICT);
        }
        validator.validarAforo(dto.getAforoMaximo());
        // No dejo bajar el cupo si ya hay más gente inscrita que el nuevo máximo
        if (dto.getAforoMaximo() < evento.getAforoActual()) {
            throw new CustomException(
                    "El aforo máximo no puede reducirse por debajo de la cantidad actual de asistentes (" + evento.getAforoActual() + ").",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private int resolverViejaDuracion(Evento evento) {
        LocalDateTime viejoInicio = evento.getFecha();
        LocalDateTime viejoFin = evento.getFechaFin();
        if (evento.getDuracionHoras() != null && evento.getDuracionHoras() > 0) {
            return evento.getDuracionHoras();
        }
        // Por si hay eventos viejos sin duracionHoras guardada, la calculo al vuelo
        return reloj.calcularDuracionHoras(viejoInicio, viejoFin == null ? viejoInicio.plusHours(1) : viejoFin);
    }

    private EventoDTO editarEventoSinCosto(Evento evento, EstadoEvento estadoAntes) {
        evento.setResumenSolicitudEdicion(null);
        evento.setEstadoPrevioRevision(null);
        if (estadoAntes == EstadoEvento.ACTIVO) {
            evento.setEstado(EstadoEvento.ACTIVO);
        } else if (estadoAntes == EstadoEvento.APROBADO) {
            evento.setEstado(EstadoEvento.APROBADO);
        } else {
            evento.setEstado(EstadoEvento.PENDIENTE);
        }
        Evento guardado = eventoRepository.save(evento);
        if (guardado.getEstado() == EstadoEvento.APROBADO) {
            return activarYAcompanarOrganizador(guardado.getId());
        }
        try {
            inscripcionService.inscribirOrganizadorEnSuEvento(guardado.getId());
        } catch (RuntimeException ignored) {
            // Si falla inscribir al organizador no tumbo toda la edición (a veces es tema de cupo)
        }
        return mapeador.conAlerta(eventoRepository.findByIdWithOrganizador(guardado.getId()).orElse(guardado), null);
    }

    @Override
    public EventoDTO aprobar(Long id) {
        Evento evento = buscarPorId(id);
        // Otra vez chequeo el salón antes de aprobar, por si alguien reservó el mismo hueco mientras tanto
        validator.assertUbicacionDisponible(
                evento.getFecha(),
                EventoVentanaUtil.instanteFin(evento),
                evento.getUbicacion(),
                evento.getId());
        if (evento.getEstado() == EstadoEvento.PENDIENTE_REVISION) {
            if (novedad.tieneRevisionPendienteConSuplementoHoras(evento.getId())) {
                Pago pago = pagoRepository
                        .findByEventoId(evento.getId())
                        .orElseThrow(() -> new CustomException(
                                "No hay registro de pago para validar el suplemento por horas adicionales.",
                                HttpStatus.BAD_REQUEST));
                if (pago.getEstado() != EstadoPago.APROBADO) {
                    throw new CustomException(
                            "El suplemento por horas adicionales requiere un pago base ya aprobado.",
                            HttpStatus.BAD_REQUEST);
                }
                finanzas.prepararComplementoPago(pago, evento.getCosto());
                evento.setEstado(EstadoEvento.PENDIENTE_SUPLEMENTO);
                return mapeador.toDto(eventoRepository.save(evento));
            }
            novedad.marcarUltimaNovedadResuelta(evento.getId(), EstadoNovedadEvento.APROBADO, null);
            EstadoEvento volver =
                    evento.getEstadoPrevioRevision() != null ? evento.getEstadoPrevioRevision() : EstadoEvento.ACTIVO;
            evento.setEstado(volver);
            evento.setEstadoPrevioRevision(null);
            evento.setResumenSolicitudEdicion(null);
            return mapeador.toDto(eventoRepository.save(evento));
        }
        if (evento.getEstado() != EstadoEvento.PENDIENTE) {
            throw new CustomException(
                    "Solo se pueden aprobar eventos en estado PENDIENTE (alta nueva) o PENDIENTE_REVISION (cambios).",
                    HttpStatus.BAD_REQUEST);
        }
        evento.setEstado(EstadoEvento.APROBADO);
        evento.setResumenSolicitudEdicion(null);
        Evento guardado = eventoRepository.save(evento);
        // Eventos gratis los activo directo sin pasar por pago
        if (guardado.getCosto() <= 0) {
            return activarYAcompanarOrganizador(guardado.getId());
        }
        return mapeador.toDto(guardado);
    }

    @Override
    public EventoDTO rechazar(Long id, String motivo) {
        Evento evento = buscarPorId(id);
        if (evento.getEstado() == EstadoEvento.PENDIENTE_REVISION) {
            EstadoEvento volver =
                    evento.getEstadoPrevioRevision() != null ? evento.getEstadoPrevioRevision() : EstadoEvento.ACTIVO;
            try {
                // Acá intento dejar el evento como estaba antes de que el organizador editara
                novedad.revertirEventoDesdeUltimaNovedad(evento);
            } catch (Exception e) {
                throw new CustomException(
                        "No se pudo revertir la edición: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            novedad.marcarUltimaNovedadResuelta(evento.getId(), EstadoNovedadEvento.RECHAZADO, motivo);
            evento.setEstado(volver);
            evento.setEstadoPrevioRevision(null);
            evento.setResumenSolicitudEdicion(null);
            evento.setMotivoRechazo(motivo != null && !motivo.isBlank() ? motivo.trim() : null);
            return mapeador.toDto(eventoRepository.save(evento));
        }
        if (evento.getEstado() != EstadoEvento.PENDIENTE) {
            throw new CustomException(
                    "Solo se pueden rechazar eventos en estado PENDIENTE (alta nueva) o PENDIENTE_REVISION (cambios).",
                    HttpStatus.BAD_REQUEST);
        }
        evento.setEstado(EstadoEvento.RECHAZADO);
        evento.setMotivoRechazo(motivo != null && !motivo.isBlank() ? motivo.trim() : null);
        return mapeador.toDto(eventoRepository.save(evento));
    }

    @Override
    public EventoDTO cancelar(Long id, Long organizadorId, String motivo) {
        Evento evento = buscarPorId(id);
        if (evento.getOrganizadorId() == null || !evento.getOrganizadorId().equals(organizadorId)) {
            throw new CustomException("Solo el organizador puede cancelar el evento.", HttpStatus.FORBIDDEN);
        }
        if (motivo == null || motivo.isBlank()) {
            throw new CustomException("El motivo de cancelación es obligatorio.", HttpStatus.BAD_REQUEST);
        }
        if (evento.getEstado() == EstadoEvento.CANCELADO) {
            throw new CustomException("El evento ya está cancelado.", HttpStatus.BAD_REQUEST);
        }
        if (evento.getEstado() == EstadoEvento.FINALIZADO) {
            throw new CustomException("No se puede cancelar un evento ya FINALIZADO.", HttpStatus.BAD_REQUEST);
        }
        if (evento.getEstado() == EstadoEvento.PENDIENTE_CANCELACION) {
            throw new CustomException("Ya existe una solicitud de cancelación pendiente de revisión.", HttpStatus.CONFLICT);
        }

        // Si todavía no estaba publicado/activo, cancelo de una sin pedirle permiso al admin
        if (evento.getEstado() == EstadoEvento.PENDIENTE) {
            evento.setEstado(EstadoEvento.CANCELADO);
            evento.setMotivoCancelacion(motivo.trim());
            evento.setEstadoPrevioRevision(null);
            Evento guardado = eventoRepository.save(evento);
            notificarInscritosCancelacion(guardado);
            return mapeador.toDto(guardado);
        }

        // Si ya estaba en marcha o con gente, la cancelación queda pendiente y el admin decide el reembolso (70%)
        if (evento.getEstado() == EstadoEvento.ACTIVO
                || evento.getEstado() == EstadoEvento.APROBADO
                || evento.getEstado() == EstadoEvento.PENDIENTE_REVISION
                || evento.getEstado() == EstadoEvento.PENDIENTE_SUPLEMENTO) {
            double valorPagado = pagoRepository.findByEventoId(id)
                    .filter((p) -> p.getEstado() == EstadoPago.APROBADO)
                    .map(Pago::getMonto)
                    .orElse(0.0);
            evento.setEstadoPrevioRevision(evento.getEstado());
            evento.setEstado(EstadoEvento.PENDIENTE_CANCELACION);
            evento.setMotivoCancelacion(motivo.trim());
            Evento guardado = eventoRepository.save(evento);
            novedad.registrarNovedadCancelacionSolicitud(guardado, organizadorId, valorPagado);
            return mapeador.conAlerta(
                    guardado,
                    "La cancelación quedó pendiente de aprobación del administrador. Si se aprueba, solo se reembolsará el 70% del valor pagado a la plataforma.");
        }

        evento.setEstado(EstadoEvento.CANCELADO);
        evento.setMotivoCancelacion(motivo.trim());
        evento.setEstadoPrevioRevision(null);
        Evento guardado = eventoRepository.save(evento);
        notificarInscritosCancelacion(guardado);
        return mapeador.toDto(guardado);
    }

    @Override
    public EventoDTO aprobarCancelacion(Long id) {
        Evento evento = buscarPorId(id);
        if (evento.getEstado() != EstadoEvento.PENDIENTE_CANCELACION) {
            throw new CustomException("Solo se puede aprobar una cancelación en estado PENDIENTE_CANCELACION.",
                    HttpStatus.BAD_REQUEST);
        }
        double valorPagado = pagoRepository.findByEventoId(id)
                .filter((p) -> p.getEstado() == EstadoPago.APROBADO)
                .map(Pago::getMonto)
                .orElse(0.0);
        novedad.marcarNovedadCancelacionAprobada(evento.getId());
        evento.setEstado(EstadoEvento.CANCELADO);
        evento.setEstadoPrevioRevision(null);
        Evento guardado = eventoRepository.save(evento);
        notificarInscritosCancelacion(guardado);
        // Le aviso al organizador cuánto pagó y cuánto le tocaría de vuelta (orientativo, 70%)
        notificacionService.crear(
                guardado.getOrganizadorId(),
                "Tu solicitud de cancelación fue aprobada. Valor pagado a la plataforma: " + EventoFinanzasHelper.copTexto(valorPagado)
                        + " COP. Monto orientativo a devolver (70%): " + EventoFinanzasHelper.copTexto(valorPagado * 0.70) + " COP.",
                TipoNotificacion.INFO);
        return mapeador.toDto(guardado);
    }

    @Override
    public EventoDTO rechazarCancelacion(Long id, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new CustomException("El motivo de rechazo es obligatorio.", HttpStatus.BAD_REQUEST);
        }
        Evento evento = buscarPorId(id);
        if (evento.getEstado() != EstadoEvento.PENDIENTE_CANCELACION) {
            throw new CustomException("Solo se puede rechazar una cancelación en estado PENDIENTE_CANCELACION.",
                    HttpStatus.BAD_REQUEST);
        }
        novedad.marcarNovedadCancelacionRechazada(evento.getId(), motivo.trim());
        EstadoEvento volver =
                evento.getEstadoPrevioRevision() != null ? evento.getEstadoPrevioRevision() : EstadoEvento.ACTIVO;
        evento.setEstado(volver);
        evento.setEstadoPrevioRevision(null);
        evento.setMotivoCancelacion(null);
        Evento guardado = eventoRepository.save(evento);
        notificacionService.crear(
                guardado.getOrganizadorId(),
                "Tu solicitud de cancelación fue rechazada. Motivo: " + motivo.trim(),
                TipoNotificacion.ALERTA);
        return mapeador.toDto(guardado);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventoDTO activarPorPago(Long id) {
        Evento evento = buscarPorId(id);
        if (evento.getEstado() != EstadoEvento.APROBADO) {
            throw new CustomException("El evento debe estar APROBADO antes de activarse.", HttpStatus.BAD_REQUEST);
        }
        // Cuando el pago queda bien, el evento ya puede salir al catálogo / inscripciones
        evento.setEstado(EstadoEvento.ACTIVO);
        return mapeador.toDto(eventoRepository.save(evento));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventoDTO activarTrasSuplementoPago(Long eventoId) {
        Evento evento = buscarPorId(eventoId);
        if (evento.getEstado() != EstadoEvento.PENDIENTE_SUPLEMENTO) {
            throw new CustomException(
                    "El evento no está pendiente de suplemento de pago.", HttpStatus.BAD_REQUEST);
        }
        EstadoEvento volver =
                evento.getEstadoPrevioRevision() != null ? evento.getEstadoPrevioRevision() : EstadoEvento.ACTIVO;
        evento.setEstado(volver);
        evento.setEstadoPrevioRevision(null);
        evento.setResumenSolicitudEdicion(null);
        novedad.marcarNovedadSuplementoAprobada(eventoId);
        return mapeador.toDto(eventoRepository.save(evento));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resolverComplementoPagoSobreEventoActivo(Long eventoId) {
        Evento evento = buscarPorId(eventoId);
        if (evento.getEstado() != EstadoEvento.ACTIVO) {
            return;
        }
        novedad.marcarNovedadSuplementoAprobada(eventoId);
    }

    @Override
    public List<EventoNovedadDTO> listarNovedades(Long eventoId) {
        return eventoNovedadRepository.findByEventoIdOrderByFechaSolicitudDesc(eventoId).stream()
                .map(novedad::toNovedadDto)
                .toList();
    }

    @Override
    public EventoDTO obtenerPorId(Long id) {
        reloj.marcarEventosActivosFinalizados();
        Evento evento = buscarPorId(id);
        if (evento.getEstado() == EstadoEvento.ACTIVO && reloj.eventoHaFinalizadoSuVentana(evento)) {
            evento.setEstado(EstadoEvento.FINALIZADO);
            eventoRepository.save(evento);
        }
        return mapeador.toDto(evento);
    }

    @Override
    public List<EventoDTO> listarTodos() {
        reloj.marcarEventosActivosFinalizados();
        return eventoRepository.findAll().stream().map(mapeador::toDto).toList();
    }

    @Override
    public List<EventoDTO> listarCatalogoPublicoActivo() {
        reloj.marcarEventosActivosFinalizados();
        // Solo públicos y ACTIVOS; los privados no los ve cualquiera en el catálogo
        return eventoRepository.findByTipoEventoAndEstado(TipoEvento.PUBLICO, EstadoEvento.ACTIVO)
                .stream()
                .filter((e) -> !reloj.eventoHaFinalizadoSuVentana(e))
                .map(mapeador::toCatalogoPublicoDto)
                .toList();
    }

    @Override
    public EventoDTO obtenerParaCatalogoPublico(Long id) {
        reloj.marcarEventosActivosFinalizados();
        Evento e = buscarPorId(id);
        if (e.getTipoEvento() != TipoEvento.PUBLICO || e.getEstado() != EstadoEvento.ACTIVO) {
            throw new CustomException("Evento no disponible en el catálogo público.", HttpStatus.NOT_FOUND);
        }
        if (reloj.eventoHaFinalizadoSuVentana(e)) {
            e.setEstado(EstadoEvento.FINALIZADO);
            eventoRepository.save(e);
            throw new CustomException("Evento no disponible en el catálogo público.", HttpStatus.NOT_FOUND);
        }
        return mapeador.toCatalogoPublicoDto(e);
    }

    @Override
    public List<EventoDTO> listarPorOrganizador(Long organizadorId) {
        reloj.marcarEventosActivosFinalizados();
        return eventoRepository.findByOrganizadorId(organizadorId).stream().map(mapeador::toDto).toList();
    }

    @Override
    public List<EventoDTO> buscar(EventoSearchCriteria c) {
        reloj.marcarEventosActivosFinalizados();
        // Armo el filtro con Specification; así el admin/organizador busca por nombre, fechas, estado, etc.
        Specification<Evento> spec = Specification.where(EventoSpecification.hasNombre(c.getNombre()))
                .and(EventoSpecification.hasCategoria(c.getCategoria()))
                .and(EventoSpecification.hasTipo(c.getTipoEvento()))
                .and(EventoSpecification.hasEstado(c.getEstado()))
                .and(EventoSpecification.fechaDesde(c.getFechaDesde()))
                .and(EventoSpecification.fechaHasta(c.getFechaHasta()))
                .and(EventoSpecification.hasOrganizador(c.getOrganizadorId()));
        return eventoRepository.findAll(spec).stream().map(mapeador::toDto).toList();
    }

    @Override
    public void aumentarAforo(Long eventoId) {
        Evento evento = buscarPorId(eventoId);
        if (evento.getAforoActual() >= evento.getAforoMaximo()) {
            throw new CustomException("El evento ha alcanzado su aforo máximo.", HttpStatus.CONFLICT);
        }
        evento.setAforoActual(evento.getAforoActual() + 1);
        eventoRepository.save(evento);
    }

    @Override
    public void disminuirAforo(Long eventoId) {
        Evento evento = buscarPorId(eventoId);
        if (evento.getAforoActual() <= 0) {
            throw new CustomException("El aforo actual no puede ser menor a 0.", HttpStatus.CONFLICT);
        }
        evento.setAforoActual(evento.getAforoActual() - 1);
        eventoRepository.save(evento);
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadSalonDTO consultarDisponibilidadSalon(
            String ubicacion,
            LocalDateTime desde,
            LocalDateTime hasta,
            Long excluirEventoId,
            LocalDateTime propuestaInicio,
            LocalDateTime propuestaFin) {
        if (desde == null || hasta == null) {
            throw new CustomException("Indica el rango de fechas (desde y hasta).", HttpStatus.BAD_REQUEST);
        }
        if (hasta.isBefore(desde)) {
            throw new CustomException("La fecha «hasta» no puede ser anterior a «desde».", HttpStatus.BAD_REQUEST);
        }
        String ub = validator.ubicacionFinal(ubicacion);
        List<Evento> candidatos = validator.listarEventosQueReservanUbicacion(ubicacion);

        DisponibilidadSalonDTO resp = new DisponibilidadSalonDTO();
        resp.setUbicacion(ub);
        resp.setDesde(desde);
        resp.setHasta(hasta);

        // Este método está largo: recorro eventos y armo las franjas ocupadas para que el front pinte el calendario
        List<FranjaOcupacionSalonDTO> franjas = new ArrayList<>();
        for (Evento e : candidatos) {
            if (excluirEventoId != null && excluirEventoId.equals(e.getId())) {
                continue;
            }
            LocalDateTime ini = e.getFecha();
            LocalDateTime fin = EventoVentanaUtil.instanteFin(e);
            if (fin.isBefore(desde) || ini.isAfter(hasta)) {
                continue;
            }
            FranjaOcupacionSalonDTO f = new FranjaOcupacionSalonDTO();
            f.setEventoId(e.getId());
            f.setNombreEvento(e.getNombre());
            f.setEstado(e.getEstado());
            f.setInicio(ini);
            f.setFin(fin);
            if (e.getOrganizadorId() != null) {
                usuarioRepository.findById(e.getOrganizadorId()).ifPresent(u -> f.setNombreOrganizador(u.getNombre()));
            }
            franjas.add(f);
        }
        franjas.sort((a, b) -> a.getInicio().compareTo(b.getInicio()));
        resp.setOcupaciones(franjas);

        // Si mandan una propuesta de horario, reviso si choca con algún otro (EventoVentanaUtil me salva con el solapamiento)
        if (propuestaInicio != null && propuestaFin != null) {
            if (!propuestaFin.isAfter(propuestaInicio)) {
                resp.setPropuestaDisponible(false);
                resp.setMensajePropuesta("La hora de fin debe ser posterior al inicio.");
            } else {
                boolean libre = true;
                StringBuilder conflicto = new StringBuilder();
                for (Evento otro : candidatos) {
                    if (excluirEventoId != null && excluirEventoId.equals(otro.getId())) {
                        continue;
                    }
                    if (EventoVentanaUtil.ventanasSeSolapan(propuestaInicio, propuestaFin, otro)) {
                        libre = false;
                        LocalDateTime otroFin = EventoVentanaUtil.instanteFin(otro);
                        conflicto.append(String.format(
                                Locale.ROOT,
                                "«%s» (%s – %s, %s). ",
                                otro.getNombre(),
                                otro.getFecha().format(EventoValidator.FMT_VENTANA),
                                otroFin.format(EventoValidator.FMT_VENTANA),
                                otro.getEstado()));
                    }
                }
                resp.setPropuestaDisponible(libre);
                resp.setMensajePropuesta(
                        libre
                                ? "El salón está libre en el horario indicado."
                                : "No disponible: ya hay evento(s) programado(s): " + conflicto);
            }
        }
        return resp;
    }

    @Override
    public void marcarEventosActivosFinalizados() {
        reloj.marcarEventosActivosFinalizados();
    }

    private static boolean estadoBloqueaEdicion(EstadoEvento e) {
        // Mientras esté en estos estados, el organizador no puede seguir editando hasta que admin o pago resuelvan
        return e == EstadoEvento.PENDIENTE_REVISION
                || e == EstadoEvento.PENDIENTE_SUPLEMENTO
                || e == EstadoEvento.PENDIENTE_CANCELACION;
    }

    private EventoDTO activarYAcompanarOrganizador(Long eventoId) {
        activarPorPago(eventoId);
        try {
            inscripcionService.inscribirOrganizadorEnSuEvento(eventoId);
        } catch (RuntimeException ignored) {
        }
        return obtenerPorId(eventoId);
    }

    private void notificarInscritosCancelacion(Evento guardado) {
        List<Inscripcion> inscripciones = inscripcionRepository.findByEventoId(guardado.getId());
        for (Inscripcion ins : inscripciones) {
            if (ins.getEstado() == EstadoInscripcion.CANCELADO) {
                continue;
            }
            // Les mando notificación a todos los que no habían cancelado solos
            notificacionService.crear(
                    ins.getUsuarioId(),
                    "El evento \"" + guardado.getNombre() + "\" fue cancelado por el organizador."
                            + (guardado.getMotivoCancelacion() != null
                                    ? " Motivo: " + guardado.getMotivoCancelacion()
                                    : ""),
                    TipoNotificacion.ALERTA);
        }
    }

    private Evento buscarPorId(Long id) {
        return eventoRepository.findByIdWithOrganizador(id)
                .orElseThrow(() -> new CustomException("No se encontró el evento con ID: " + id, HttpStatus.NOT_FOUND));
    }
}
