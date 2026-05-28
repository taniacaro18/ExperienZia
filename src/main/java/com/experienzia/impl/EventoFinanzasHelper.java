package com.experienzia.impl;

import com.experienzia.dto.EventoDTO;
import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.EstadoPago;
import com.experienzia.entity.Evento;
import com.experienzia.entity.Pago;
import com.experienzia.entity.TipoNovedadEvento;
import com.experienzia.repository.EventoRepository;
import com.experienzia.repository.PagoRepository;
import com.experienzia.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Plata, suplementos y comprobantes cuando el organizador edita horas o tarifa del evento.
@Component
public class EventoFinanzasHelper {

    // 5% por cada hora que quiten si el evento ya estaba pagado — regla de negocio fea pero así la pidieron.
    public static final double PENALIZACION_POR_HORA_REDUCIDA = 0.05;

    private final PagoRepository pagoRepository;
    private final FileStorageService fileStorageService;
    private final EventoRepository eventoRepository;
    private final EventoNovedadService novedad;
    private final EventoMapeadorHelper mapeador;

    // Precio por hora sale del properties; si no está, 100k COP por defecto.
    @Value("${experienzia.precio-por-hora:100000}")
    private double precioPorHora;

    public EventoFinanzasHelper(
            PagoRepository pagoRepository,
            FileStorageService fileStorageService,
            EventoRepository eventoRepository,
            EventoNovedadService novedad,
            EventoMapeadorHelper mapeador) {
        this.pagoRepository = pagoRepository;
        this.fileStorageService = fileStorageService;
        this.eventoRepository = eventoRepository;
        this.novedad = novedad;
        this.mapeador = mapeador;
    }

    public double calcularCosto(int duracionHoras) {
        if (duracionHoras <= 0) {
            return 0;
        }
        return precioPorHora * duracionHoras;
    }

    public double calcularPenalizacionReduccionHoras(double baseMonto, int horasReducidas) {
        return baseMonto * PENALIZACION_POR_HORA_REDUCIDA * horasReducidas;
    }

    // Si cambió el monto hay que borrar el PDF viejo del storage y pedir comprobante nuevo.
    public void limpiarComprobantePago(Pago p) {
        String url = p.getComprobanteUrl();
        if (url != null && !url.isBlank()) {
            fileStorageService.borrarComprobantePublico(url);
        }
        p.setComprobanteUrl(null);
    }

    // Pago ya aprobado pero el evento costó más: dejo solo el delta en PENDIENTE y pido comprobante otra vez.
    public void prepararComplementoPago(Pago p, double nuevoCostoEvento) {
        if (p.getEstado() != EstadoPago.APROBADO) {
            return;
        }
        double montoPrevio = p.getMonto();
        // El 0.01 es por redondeos de double — sin eso a veces cree que hay diferencia fantasma.
        if (nuevoCostoEvento <= montoPrevio + 0.01) {
            return;
        }
        double delta = nuevoCostoEvento - montoPrevio;
        p.setSaldoAprobadoPrevio(montoPrevio);
        p.setMonto(delta);
        p.setEstado(EstadoPago.PENDIENTE);
        limpiarComprobantePago(p);
        p.setMotivoRechazo(null);
        p.setAprobadorId(null);
        p.setFechaResolucion(null);
        pagoRepository.save(p);
    }

    // Texto que ve el admin en la cola de revisión: qué campos tocó el organizador.
    public static String construirResumenEdicion(
            int viejaDuracion,
            int nuevaDuracion,
            double viejoCosto,
            double nuevoCosto,
            boolean cambiaNombre,
            boolean cambiaDescripcion,
            boolean cambiaImagen,
            boolean cambiaUbicacion,
            boolean cambiaAforo,
            boolean cambiaTipo,
            boolean cambiaCategoria,
            boolean cambiaDuracion,
            boolean cambiaAgenda) {
        List<String> partes = new ArrayList<>();
        if (cambiaNombre) {
            partes.add("Nombre");
        }
        if (cambiaDescripcion) {
            partes.add("Descripción");
        }
        if (cambiaImagen) {
            partes.add("Imagen");
        }
        if (cambiaUbicacion) {
            partes.add("Ubicación");
        }
        if (cambiaAforo) {
            partes.add("Aforo máximo");
        }
        if (cambiaTipo) {
            partes.add("Modalidad (público/privado)");
        }
        if (cambiaCategoria) {
            partes.add("Categoría");
        }
        if (cambiaDuracion || Math.abs(nuevoCosto - viejoCosto) > 0.01) {
            partes.add(String.format(Locale.ROOT,
                    "Duración/tarifa: %d h (%s COP) → %d h (%s COP)",
                    viejaDuracion, copTexto(viejoCosto), nuevaDuracion, copTexto(nuevoCosto)));
        } else if (cambiaAgenda) {
            partes.add("Fechas u horario del evento (misma duración facturada)");
        }
        if (partes.isEmpty()) {
            return "Edición de solicitud (revisar datos del evento).";
        }
        return String.join(" · ", partes);
    }

    public static String copTexto(double v) {
        return String.format(Locale.ROOT, "%.0f", v);
    }

    // Si bajan horas con pago ya hecho: penalización + PENDIENTE_REVISION y alerta al front.
    public Optional<EventoDTO> procesarDisminucionHoras(EventoEdicionFlujoContext ctx) {
        if (!ctx.gestionadoActivoOaprobado() || !ctx.cambios().disminuyeHoras()) {
            return Optional.empty();
        }
        Evento evento = ctx.evento();
        int horasRed = ctx.viejaDuracion() - ctx.nuevaDuracion();
        double baseMonto = ctx.pagoAprobado() ? ctx.pagoOpt().orElseThrow().getMonto() : ctx.viejoCosto();
        double penal = calcularPenalizacionReduccionHoras(baseMonto, horasRed);
        evento.setEstadoPrevioRevision(ctx.estadoAntes());
        evento.setEstado(EstadoEvento.PENDIENTE_REVISION);
        evento.setResumenSolicitudEdicion(
                resumenEdicion(ctx, ctx.viejoCosto(), ctx.costoFinal(), true)
                        + " · Penalización estimada (5% por hora reducida): " + copTexto(penal) + " COP.");
        Evento guardado = eventoRepository.save(evento);
        novedad.registrarNovedadDisminucionHoras(
                guardado, ctx.dto().getOrganizadorId(), ctx.viejaDuracion(), ctx.nuevaDuracion(), penal, ctx.snapshotAntes());
        String msg = "Se aplicará una penalización del 5% por cada hora reducida. No se reembolsa el valor completo del pago. "
                + "El administrador debe aprobar el cambio.";
        return Optional.of(mapeador.conAlerta(guardado, msg));
    }

    // Cambio de duración cuando el evento ya estaba ACTIVO/APROBADO — aquí se enredan los estados del pago.
    public Optional<EventoDTO> procesarCambiosDuracionConPago(EventoEdicionFlujoContext ctx) {
        CambiosEdicionEvento c = ctx.cambios();
        if (!c.cambiaDuracion()
                || (ctx.estadoAntes() != EstadoEvento.APROBADO && ctx.estadoAntes() != EstadoEvento.ACTIVO)) {
            return Optional.empty();
        }
        Evento evento = ctx.evento();
        var pOpt = ctx.pagoOpt();

        // Primer comprobante aún no aprobado: solo actualizo monto y a veces pido PDF nuevo.
        if (pOpt.isPresent() && pOpt.get().getEstado() == EstadoPago.PENDIENTE
                && pOpt.get().getSaldoAprobadoPrevio() == null) {
            Pago pp = pOpt.get();
            boolean tuvoCambioTarifa = Math.abs(ctx.costoFinal() - ctx.viejoCosto()) > 0.01;
            pp.setMonto(ctx.costoFinal());
            String msgTarifa = null;
            if (tuvoCambioTarifa) {
                limpiarComprobantePago(pp);
                msgTarifa =
                        "Cambió la tarifa del evento: debes subir un nuevo comprobante en la sección Pagos antes de que el administrador lo valide.";
            }
            pagoRepository.save(pp);
            evento.setEstado(ctx.estadoAntes());
            evento.setResumenSolicitudEdicion(null);
            evento.setEstadoPrevioRevision(null);
            return Optional.of(mapeador.conAlerta(eventoRepository.save(evento), msgTarifa));
        }

        boolean necesitaRevisionEvento = pOpt.isEmpty() || pOpt.get().getEstado() == EstadoPago.RECHAZADO;
        if (necesitaRevisionEvento) {
            evento.setEstado(EstadoEvento.PENDIENTE);
            evento.setEstadoPrevioRevision(null);
            evento.setResumenSolicitudEdicion(resumenEdicion(ctx, ctx.viejoCosto(), ctx.costoFinal(), c.cambiaDuracion()));
            return Optional.of(mapeador.conAlerta(eventoRepository.save(evento), null));
        }

        // Suplemento: ya pagó una parte y falta validar el comprobante del resto.
        if (pOpt.get().getEstado() == EstadoPago.PENDIENTE && pOpt.get().getSaldoAprobadoPrevio() != null) {
            evento.setEstado(EstadoEvento.PENDIENTE_SUPLEMENTO);
            if (evento.getEstadoPrevioRevision() == null) {
                evento.setEstadoPrevioRevision(ctx.estadoAntes());
            }
            return Optional.of(mapeador.conAlerta(
                    eventoRepository.save(evento),
                    "Tienes un pago adicional pendiente de validación. Completa el comprobante o espera al administrador."));
        }

        if (pOpt.get().getEstado() == EstadoPago.APROBADO) {
            evento.setEstadoPrevioRevision(ctx.estadoAntes());
            evento.setEstado(EstadoEvento.PENDIENTE_REVISION);
            evento.setResumenSolicitudEdicion(resumenEdicion(ctx, ctx.viejoCosto(), ctx.costoFinal(), c.cambiaDuracion()));
            Evento guardado = eventoRepository.save(evento);
            novedad.registrarNovedadEdicionBasica(
                    guardado,
                    ctx.dto().getOrganizadorId(),
                    TipoNovedadEvento.EDICION_METADATOS,
                    ctx.snapshotAntes(),
                    guardado.getResumenSolicitudEdicion());
            return Optional.of(mapeador.conAlerta(
                    guardado,
                    "Cambio de duración con pago ya aprobado: queda a revisión administrativa para validar tarifa y estado."));
        }

        evento.setEstado(ctx.estadoAntes());
        evento.setResumenSolicitudEdicion(null);
        evento.setEstadoPrevioRevision(null);
        return Optional.of(mapeador.conAlerta(eventoRepository.save(evento), null));
    }

    // Si ningún flujo especial aplicó, el evento vuelve a PENDIENTE con resumen para el admin.
    public EventoDTO finalizarEdicionPendiente(EventoEdicionFlujoContext ctx) {
        Evento evento = ctx.evento();
        CambiosEdicionEvento c = ctx.cambios();
        evento.setEstado(EstadoEvento.PENDIENTE);
        evento.setEstadoPrevioRevision(null);
        evento.setResumenSolicitudEdicion(resumenEdicion(ctx, ctx.viejoCosto(), ctx.costoFinal(), c.cambiaDuracion()));
        return mapeador.conAlerta(eventoRepository.save(evento), null);
    }

    private String resumenEdicion(
            EventoEdicionFlujoContext ctx, double refViejoCosto, double refNuevoCosto, boolean marcarCambioDuracion) {
        CambiosEdicionEvento c = ctx.cambios();
        return construirResumenEdicion(
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
}
