package com.experienzia.impl;

import com.experienzia.dto.EventoDTO;
import com.experienzia.dto.EventoNovedadDTO;
import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.EstadoNovedadEvento;
import com.experienzia.entity.Evento;
import com.experienzia.entity.EventoNovedad;
import com.experienzia.entity.Pago;
import com.experienzia.entity.TipoEvento;
import com.experienzia.entity.TipoNovedadEvento;
import com.experienzia.entity.TipoNotificacion;
import com.experienzia.exceptions.CustomException;
import com.experienzia.repository.EventoNovedadRepository;
import com.experienzia.repository.EventoRepository;
import com.experienzia.service.NotificacionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// Historial de cambios “sensibles” del evento (horas, tipo, cancelación) en JSON para que el admin apruebe o revierta.
@Service
public class EventoNovedadService {

    private final EventoNovedadRepository eventoNovedadRepository;
    private final EventoRepository eventoRepository;
    private final ObjectMapper objectMapper;
    private final EventoMapeadorHelper mapeador;
    private final NotificacionService notificacionService;

    public EventoNovedadService(
            EventoNovedadRepository eventoNovedadRepository,
            EventoRepository eventoRepository,
            ObjectMapper objectMapper,
            EventoMapeadorHelper mapeador,
            NotificacionService notificacionService) {
        this.eventoNovedadRepository = eventoNovedadRepository;
        this.eventoRepository = eventoRepository;
        this.objectMapper = objectMapper;
        this.mapeador = mapeador;
        this.notificacionService = notificacionService;
    }

    // Más horas después de pagar: casi siempre PENDIENTE_REVISION y mensaje de cuánto falta pagar.
    // Optional vacío = este flujo no aplicó y EventoServiceImpl sigue con el siguiente paso.
    public Optional<EventoDTO> procesarAumentoHoras(EventoEdicionFlujoContext ctx) {
        if (!ctx.gestionadoActivoOaprobado() || !ctx.cambios().aumentaHoras() || !ctx.pagoAprobado()) {
            return Optional.empty();
        }
        Evento evento = ctx.evento();
        Pago p = ctx.pagoOpt().orElseThrow();
        double montoYaCobradoAprobado = p.getMonto();

        if (ctx.costoFinal() > montoYaCobradoAprobado + 0.01) {
            evento.setEstadoPrevioRevision(ctx.estadoAntes());
            evento.setEstado(EstadoEvento.PENDIENTE_REVISION);
            evento.setResumenSolicitudEdicion(resumenEdicion(ctx, montoYaCobradoAprobado, ctx.costoFinal(), true));
            Evento guardado = eventoRepository.save(evento);
            registrarNovedadAumentoHoras(
                    guardado,
                    ctx.dto().getOrganizadorId(),
                    ctx.viejaDuracion(),
                    ctx.nuevaDuracion(),
                    p,
                    ctx.costoFinal(),
                    ctx.snapshotAntes());
            avisarAdminRevisionPendiente(guardado);
            double adicional = ctx.costoFinal() - montoYaCobradoAprobado;
            String msg = "Aumentaste las horas: un administrador debe aprobar los cambios primero. Después deberás pagar solo el excedente ("
                    + EventoFinanzasHelper.copTexto(adicional)
                    + " COP) y subir un comprobante por esa diferencia.";
            return Optional.of(mapeador.conAlerta(guardado, msg));
        }

        evento.setEstadoPrevioRevision(ctx.estadoAntes());
        evento.setEstado(EstadoEvento.PENDIENTE_REVISION);
        evento.setResumenSolicitudEdicion(resumenEdicion(ctx, ctx.viejoCosto(), ctx.costoFinal(), true));
        Evento guardado = eventoRepository.save(evento);
        registrarNovedadEdicionBasica(
                guardado,
                ctx.dto().getOrganizadorId(),
                TipoNovedadEvento.AUMENTO_HORAS,
                ctx.snapshotAntes(),
                guardado.getResumenSolicitudEdicion());
        avisarAdminRevisionPendiente(guardado);
        return Optional.of(mapeador.conAlerta(
                guardado,
                "Aumentaste horas respecto al pago ya aprobado, pero el sistema no detecta saldo adicional pendiente. "
                        + "Los cambios quedaron a revisión administrativa para validar tarifa y estado del pago."));
    }

    // Cambió nombre, foto, tipo, etc. en evento ya publicado → cola de revisión sin cobrar de más (salvo horas).
    public Optional<EventoDTO> procesarRevisionMetadatos(EventoEdicionFlujoContext ctx) {
        if (!ctx.gestionadoActivoOaprobado()) {
            return Optional.empty();
        }
        CambiosEdicionEvento c = ctx.cambios();
        if (!c.requiereRevisionMetadatos() && !c.requiereRevisionTipoCat()) {
            return Optional.empty();
        }
        Evento evento = ctx.evento();
        evento.setEstadoPrevioRevision(ctx.estadoAntes());
        evento.setEstado(EstadoEvento.PENDIENTE_REVISION);
        TipoNovedadEvento tipoNov = c.requiereRevisionTipoCat()
                ? TipoNovedadEvento.EDICION_TIPO_CATEGORIA
                : TipoNovedadEvento.EDICION_METADATOS;
        evento.setResumenSolicitudEdicion(resumenEdicion(ctx, ctx.viejoCosto(), ctx.costoFinal(), c.cambiaDuracion()));
        Evento guardado = eventoRepository.save(evento);
        registrarNovedadEdicionBasica(
                guardado, ctx.dto().getOrganizadorId(), tipoNov, ctx.snapshotAntes(), guardado.getResumenSolicitudEdicion());
        avisarAdminRevisionPendiente(guardado);
        String msg = c.requiereRevisionTipoCat()
                ? "Cambiaste modalidad o categoría: el evento queda pendiente de aprobación administrativa (sin nuevo pago salvo reglas de horas)."
                : "Los cambios quedaron pendientes de aprobación del administrador. No se solicita nuevo pago mientras no aumente la duración facturada.";
        return Optional.of(mapeador.conAlerta(guardado, msg));
    }

    private String resumenEdicion(
            EventoEdicionFlujoContext ctx, double refViejoCosto, double refNuevoCosto, boolean marcarCambioDuracion) {
        CambiosEdicionEvento c = ctx.cambios();
        return EventoFinanzasHelper.construirResumenEdicion(
                ctx.viejaDuracion(),
                ctx.nuevaDuracion(),
                refViejoCosto,
                refNuevoCosto,
                c.cambiaNombre(),
                c.cambiaDescripcion(),
                c.cambiaImagen(),
                c.cambiaUbicacion(),
                c.cambiaAforo(),
                c.cambiaTipo(),
                c.cambiaCategoria(),
                marcarCambioDuracion,
                c.cambiaAgenda());
    }

    // Copia ligera del evento antes de editar; si el admin rechaza, vuelvo a esto.
    // Copia del evento antes de editar por si hay que revertir al rechazar la novedad.
    public Map<String, Object> snapshotEventoParaRevert(Evento e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nombre", e.getNombre());
        m.put("descripcion", e.getDescripcion());
        m.put("fecha", e.getFecha() != null ? e.getFecha().toString() : null);
        m.put("fechaFin", e.getFechaFin() != null ? e.getFechaFin().toString() : null);
        m.put("ubicacion", e.getUbicacion());
        m.put("aforoMaximo", e.getAforoMaximo());
        m.put("tipoEvento", e.getTipoEvento() != null ? e.getTipoEvento().name() : null);
        m.put("categoria", e.getCategoria());
        m.put("duracionHoras", e.getDuracionHoras());
        m.put("costo", e.getCosto());
        m.put("imagen", e.getImagen());
        return m;
    }

    // Restauro campo por campo desde el JSON — tedioso pero explícito para no romper tipos.
    // Restauro campo por campo desde el JSON guardado — fechas las parseo a mano y rezo que vengan bien.
    public void aplicarSnapshotEvento(Evento evento, JsonNode antes) throws Exception {
        if (antes == null || antes.isNull()) {
            return;
        }
        if (antes.hasNonNull("nombre")) {
            evento.setNombre(antes.get("nombre").asText());
        }
        if (antes.hasNonNull("descripcion")) {
            evento.setDescripcion(antes.get("descripcion").asText());
        }
        // Las fechas van como string ISO en el snapshot; parse me confunde si el front manda otro formato.
        if (antes.hasNonNull("fecha")) {
            evento.setFecha(LocalDateTime.parse(antes.get("fecha").asText()));
        }
        if (antes.hasNonNull("fechaFin")) {
            evento.setFechaFin(LocalDateTime.parse(antes.get("fechaFin").asText()));
        }
        if (antes.hasNonNull("ubicacion")) {
            evento.setUbicacion(antes.get("ubicacion").asText());
        }
        if (antes.has("aforoMaximo")) {
            evento.setAforoMaximo(antes.get("aforoMaximo").asInt());
        }
        if (antes.hasNonNull("tipoEvento")) {
            evento.setTipoEvento(TipoEvento.valueOf(antes.get("tipoEvento").asText()));
        }
        if (antes.has("categoria")) {
            evento.setCategoria(antes.get("categoria").isNull() ? null : antes.get("categoria").asText());
        }
        if (antes.has("duracionHoras")) {
            evento.setDuracionHoras(antes.get("duracionHoras").asInt());
        }
        if (antes.has("costo")) {
            evento.setCosto(antes.get("costo").asDouble());
        }
        if (antes.has("imagen")) {
            evento.setImagen(antes.get("imagen").isNull() ? null : antes.get("imagen").asText());
        }
    }

    // Guardo en BD la novedad con detalleJson para el panel admin.
    public void registrarNovedadEdicionBasica(
            Evento evento,
            Long orgId,
            TipoNovedadEvento tipo,
            Map<String, Object> eventoAntes,
            String resumen) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            if (evento.getEstadoPrevioRevision() != null) {
                root.put("estadoPrevio", evento.getEstadoPrevioRevision().name());
            }
            root.set("eventoAntes", objectMapper.valueToTree(eventoAntes));
            root.put("resumen", resumen != null ? resumen : "");
            EventoNovedad n = new EventoNovedad();
            n.setEventoId(evento.getId());
            n.setUsuarioSolicitanteId(orgId);
            n.setTipo(tipo);
            n.setEstado(EstadoNovedadEvento.PENDIENTE);
            n.setFechaSolicitud(LocalDateTime.now());
            n.setDetalleJson(objectMapper.writeValueAsString(root));
            eventoNovedadRepository.save(n);
        } catch (Exception ex) {
            throw new CustomException("No se pudo registrar la novedad del evento.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void registrarNovedadAumentoHoras(
            Evento evento,
            Long orgId,
            int horasAntes,
            int horasDespues,
            Pago p,
            double nuevoCostoEvento,
            Map<String, Object> snapshotAntes) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put(
                    "estadoPrevio",
                    evento.getEstadoPrevioRevision() != null ? evento.getEstadoPrevioRevision().name() : "");
            root.set("eventoAntes", objectMapper.valueToTree(snapshotAntes));
            root.put("horasAntes", horasAntes);
            root.put("horasDespues", horasDespues);
            double montoPrevio = p.getSaldoAprobadoPrevio() != null ? p.getSaldoAprobadoPrevio() : p.getMonto();
            root.put("montoPagadoPrevio", montoPrevio);
            root.put("montoAdicional", Math.max(0, nuevoCostoEvento - montoPrevio));
            EventoNovedad n = new EventoNovedad();
            n.setEventoId(evento.getId());
            n.setUsuarioSolicitanteId(orgId);
            n.setTipo(TipoNovedadEvento.AUMENTO_HORAS);
            n.setEstado(EstadoNovedadEvento.PENDIENTE);
            n.setFechaSolicitud(LocalDateTime.now());
            n.setDetalleJson(objectMapper.writeValueAsString(root));
            eventoNovedadRepository.save(n);
        } catch (Exception ex) {
            throw new CustomException("No se pudo registrar la novedad del evento.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void registrarNovedadDisminucionHoras(
            Evento evento,
            Long orgId,
            int horasAntes,
            int horasDespues,
            double penalizacion,
            Map<String, Object> snapshotAntes) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put(
                    "estadoPrevio",
                    evento.getEstadoPrevioRevision() != null ? evento.getEstadoPrevioRevision().name() : "");
            root.set("eventoAntes", objectMapper.valueToTree(snapshotAntes));
            root.put("horasAntes", horasAntes);
            root.put("horasDespues", horasDespues);
            root.put("horasReducidas", horasAntes - horasDespues);
            root.put("penalizacionEstimada", penalizacion);
            EventoNovedad n = new EventoNovedad();
            n.setEventoId(evento.getId());
            n.setUsuarioSolicitanteId(orgId);
            n.setTipo(TipoNovedadEvento.DISMINUCION_HORAS);
            n.setEstado(EstadoNovedadEvento.PENDIENTE);
            n.setFechaSolicitud(LocalDateTime.now());
            n.setDetalleJson(objectMapper.writeValueAsString(root));
            eventoNovedadRepository.save(n);
        } catch (Exception ex) {
            throw new CustomException("No se pudo registrar la novedad del evento.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void registrarNovedadCancelacionSolicitud(Evento evento, Long orgId, double valorPagado) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put(
                    "estadoPrevio",
                    evento.getEstadoPrevioRevision() != null ? evento.getEstadoPrevioRevision().name() : "");
            root.put("motivo", evento.getMotivoCancelacion());
            root.put("valorPagadoPlataforma", valorPagado);
            root.put("reembolsoPropuesto70", valorPagado * 0.70);
            EventoNovedad n = new EventoNovedad();
            n.setEventoId(evento.getId());
            n.setUsuarioSolicitanteId(orgId);
            n.setTipo(TipoNovedadEvento.CANCELACION_SOLICITUD);
            n.setEstado(EstadoNovedadEvento.PENDIENTE);
            n.setFechaSolicitud(LocalDateTime.now());
            n.setDetalleJson(objectMapper.writeValueAsString(root));
            eventoNovedadRepository.save(n);
            notificacionService.notificarAdministradores(
                    "Solicitud de cancelación del evento «" + evento.getNombre() + "» pendiente de aprobación.",
                    TipoNotificacion.ALERTA);
        } catch (Exception ex) {
            throw new CustomException("No se pudo registrar la novedad del evento.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean tieneRevisionPendienteConSuplementoHoras(Long eventoId) {
        return eventoNovedadRepository
                .findFirstByEventoIdAndEstadoAndTipoOrderByFechaSolicitudDesc(
                        eventoId, EstadoNovedadEvento.PENDIENTE, TipoNovedadEvento.AUMENTO_HORAS)
                .isPresent();
    }

    public void marcarUltimaNovedadResuelta(Long eventoId, EstadoNovedadEvento resolucion, String motivo) {
        eventoNovedadRepository
                .findFirstByEventoIdAndEstadoOrderByFechaSolicitudDesc(eventoId, EstadoNovedadEvento.PENDIENTE)
                .ifPresent(n -> {
                    n.setEstado(resolucion);
                    n.setFechaResolucion(LocalDateTime.now());
                    n.setMotivoResolucion(motivo);
                    eventoNovedadRepository.save(n);
                });
    }

    // Si rechazan la edición, leo eventoAntes del JSON de la última novedad pendiente.
    // Admin rechazó: vuelvo el evento al snapshot de la última novedad pendiente.
    public void revertirEventoDesdeUltimaNovedad(Evento evento) throws Exception {
        Optional<EventoNovedad> nov = eventoNovedadRepository
                .findFirstByEventoIdAndEstadoOrderByFechaSolicitudDesc(evento.getId(), EstadoNovedadEvento.PENDIENTE);
        if (nov.isEmpty()) {
            return;
        }
        JsonNode root = objectMapper.readTree(nov.get().getDetalleJson());
        if (root.has("eventoAntes")) {
            aplicarSnapshotEvento(evento, root.get("eventoAntes"));
        }
    }

    public void marcarNovedadSuplementoAprobada(Long eventoId) {
        eventoNovedadRepository
                .findFirstByEventoIdAndEstadoAndTipoOrderByFechaSolicitudDesc(
                        eventoId, EstadoNovedadEvento.PENDIENTE, TipoNovedadEvento.AUMENTO_HORAS)
                .ifPresent(n -> {
                    n.setEstado(EstadoNovedadEvento.APROBADO);
                    n.setFechaResolucion(LocalDateTime.now());
                    eventoNovedadRepository.save(n);
                });
    }

    public void marcarNovedadCancelacionAprobada(Long eventoId) {
        eventoNovedadRepository
                .findFirstByEventoIdAndEstadoAndTipoOrderByFechaSolicitudDesc(
                        eventoId, EstadoNovedadEvento.PENDIENTE, TipoNovedadEvento.CANCELACION_SOLICITUD)
                .ifPresent(n -> {
                    n.setEstado(EstadoNovedadEvento.APROBADO);
                    n.setFechaResolucion(LocalDateTime.now());
                    eventoNovedadRepository.save(n);
                });
    }

    public void marcarNovedadCancelacionRechazada(Long eventoId, String motivo) {
        eventoNovedadRepository
                .findFirstByEventoIdAndEstadoAndTipoOrderByFechaSolicitudDesc(
                        eventoId, EstadoNovedadEvento.PENDIENTE, TipoNovedadEvento.CANCELACION_SOLICITUD)
                .ifPresent(n -> {
                    n.setEstado(EstadoNovedadEvento.RECHAZADO);
                    n.setFechaResolucion(LocalDateTime.now());
                    n.setMotivoResolucion(motivo);
                    eventoNovedadRepository.save(n);
                });
    }

    private void avisarAdminRevisionPendiente(Evento evento) {
        notificacionService.notificarAdministradores(
                "El evento «" + evento.getNombre() + "» tiene cambios pendientes de revisión administrativa.",
                TipoNotificacion.ALERTA);
    }

    public EventoNovedadDTO toNovedadDto(EventoNovedad n) {
        EventoNovedadDTO d = new EventoNovedadDTO();
        d.setId(n.getId());
        d.setEventoId(n.getEventoId());
        d.setUsuarioSolicitanteId(n.getUsuarioSolicitanteId());
        d.setTipo(n.getTipo());
        d.setEstado(n.getEstado());
        d.setFechaSolicitud(n.getFechaSolicitud());
        d.setFechaResolucion(n.getFechaResolucion());
        d.setMotivoResolucion(n.getMotivoResolucion());
        d.setDetalleJson(n.getDetalleJson());
        return d;
    }
}
