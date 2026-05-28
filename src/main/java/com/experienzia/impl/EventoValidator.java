package com.experienzia.impl;

import com.experienzia.dto.EventoDTO;
import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.Evento;
import com.experienzia.exceptions.CustomException;
import com.experienzia.repository.EventoRepository;
import com.experienzia.util.EventoVentanaUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

// Freno creación/edición de eventos: aforo, fechas raras y choque de salones.
@Component
public class EventoValidator {

    // Tope que pusimos en el negocio — el front también debería respetarlo.
    public static final int AFORO_MAXIMO_PERMITIDO = 600;

    // Si el organizador deja ubicación vacía, asumo salón principal.
    public static final String UBICACION_POR_DEFECTO = "Salón principal";

    // Estos estados “ocupan” el salón aunque el evento no esté ACTIVO todavía.
    public static final List<EstadoEvento> ESTADOS_RESERVAN_UBICACION = List.of(
            EstadoEvento.PENDIENTE,
            EstadoEvento.APROBADO,
            EstadoEvento.ACTIVO,
            EstadoEvento.PENDIENTE_REVISION,
            EstadoEvento.PENDIENTE_SUPLEMENTO,
            EstadoEvento.PENDIENTE_CANCELACION);

    public static final DateTimeFormatter FMT_VENTANA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CO"));

    private final EventoRepository eventoRepository;

    public EventoValidator(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public void validarAforo(Integer aforo) {
        if (aforo == null || aforo <= 0) {
            throw new CustomException("El aforo máximo debe ser mayor a 0.", HttpStatus.BAD_REQUEST);
        }
        if (aforo > AFORO_MAXIMO_PERMITIDO) {
            throw new CustomException(
                    "El aforo máximo no puede ser mayor a " + AFORO_MAXIMO_PERMITIDO + " personas.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    // Evento nocturno: si fin <= inicio en el mismo día, sumo un día al fin (22:00 → 04:00).
    public LocalDateTime ajustarFinCruceMedianoche(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            return fin;
        }
        if (fin.isAfter(inicio)) {
            return fin;
        }
        return fin.plusDays(1);
    }

    public void validarFechas(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null) {
            throw new CustomException("La fecha de inicio es requerida.", HttpStatus.BAD_REQUEST);
        }
        if (fin == null) {
            throw new CustomException("La fecha de finalización es requerida.", HttpStatus.BAD_REQUEST);
        }
        if (!fin.isAfter(inicio)) {
            throw new CustomException(
                    "La fecha de finalización debe ser posterior a la fecha de inicio.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    public String ubicacionFinal(String ubicacion) {
        return (ubicacion == null || ubicacion.isBlank()) ? UBICACION_POR_DEFECTO : ubicacion.trim();
    }

    // Dos eventos no pueden usar el mismo salón a la vez — mando error con nombre del que choca.
    public void assertUbicacionDisponible(
            LocalDateTime inicio, LocalDateTime fin, String ubicacion, Long excluirEventoId) {
        Optional<Evento> conflicto = buscarConflictoUbicacion(inicio, fin, ubicacion, excluirEventoId);
        if (conflicto.isEmpty()) {
            return;
        }
        Evento otro = conflicto.get();
        String ub = ubicacionFinal(ubicacion);
        LocalDateTime otroFin = EventoVentanaUtil.instanteFin(otro);
        throw new CustomException(
                String.format(
                        Locale.ROOT,
                        "La ubicación «%s» no está disponible entre %s y %s. "
                                + "Ya existe el evento «%s» programado de %s a %s (estado %s). "
                                + "Elige otra fecha, otro horario u otra ubicación.",
                        ub,
                        inicio.format(FMT_VENTANA),
                        fin.format(FMT_VENTANA),
                        otro.getNombre(),
                        otro.getFecha().format(FMT_VENTANA),
                        otroFin.format(FMT_VENTANA),
                        otro.getEstado()),
                HttpStatus.CONFLICT);
    }

    // Busco el primer evento que se monta en el horario; EventoVentanaUtil hace el cálculo feo de solape.
    public Optional<Evento> buscarConflictoUbicacion(
            LocalDateTime inicio, LocalDateTime fin, String ubicacion, Long excluirEventoId) {
        String ub = ubicacionFinal(ubicacion);
        List<Evento> existentes =
                eventoRepository.findByUbicacionNormalizadaYEstadoIn(ub, ESTADOS_RESERVAN_UBICACION);
        for (Evento otro : existentes) {
            if (excluirEventoId != null && excluirEventoId.equals(otro.getId())) {
                continue;
            }
            if (EventoVentanaUtil.ventanasSeSolapan(inicio, fin, otro)) {
                return Optional.of(otro);
            }
        }
        return Optional.empty();
    }

    public List<Evento> listarEventosQueReservanUbicacion(String ubicacion) {
        String ub = ubicacionFinal(ubicacion);
        return eventoRepository.findByUbicacionNormalizadaYEstadoIn(ub, ESTADOS_RESERVAN_UBICACION);
    }

    // Comparo BD vs lo que mandó el PUT y armo los flags de CambiosEdicionEvento.
    // Comparo evento en BD vs DTO del PUT y armo el record CambiosEdicionEvento con todos los flags.
    public CambiosEdicionEvento evaluarCambiosEdicion(
            Evento evento,
            EventoDTO dto,
            LocalDateTime inicio,
            LocalDateTime finAjustado,
            int nuevaDuracion,
            int viejaDuracion) {
        String ubicNueva = ubicacionFinal(dto.getUbicacion());
        boolean cambiaNombre = cambiaTexto(evento.getNombre(), dto.getNombre());
        boolean cambiaDescripcion = cambiaTexto(evento.getDescripcion(), dto.getDescripcion());
        boolean cambiaImagen = cambiaTexto(evento.getImagen(), dto.getImagen());
        boolean cambiaUbicacion = cambiaTexto(evento.getUbicacion(), ubicNueva);
        boolean cambiaAforo = !Objects.equals(evento.getAforoMaximo(), dto.getAforoMaximo());
        boolean cambiaTipo = dto.getTipoEvento() != null && dto.getTipoEvento() != evento.getTipoEvento();
        // Normalizo categoría en mayúsculas para no marcar cambio por "arte" vs "ARTE".
        boolean cambiaCategoria = dto.getCategoria() != null
                && !normalizarCategoria(dto.getCategoria()).equalsIgnoreCase(normalizarCategoria(evento.getCategoria()));
        boolean cambiaDuracion = nuevaDuracion != viejaDuracion;
        LocalDateTime viejoInicio = evento.getFecha();
        LocalDateTime viejoFin = evento.getFechaFin();
        boolean cambiaAgenda = !inicio.equals(viejoInicio) || !finAjustado.equals(viejoFin);
        boolean requiereRevisionTipoCat = cambiaTipo || cambiaCategoria;
        boolean soloMetadatosSinTipoCatNiHoras = !requiereRevisionTipoCat && !cambiaDuracion;
        boolean aumentaHoras = nuevaDuracion > viejaDuracion;
        boolean disminuyeHoras = nuevaDuracion < viejaDuracion;
        return new CambiosEdicionEvento(
                ubicNueva,
                cambiaNombre,
                cambiaDescripcion,
                cambiaImagen,
                cambiaUbicacion,
                cambiaAforo,
                cambiaTipo,
                cambiaCategoria,
                cambiaDuracion,
                cambiaAgenda,
                requiereRevisionTipoCat,
                soloMetadatosSinTipoCatNiHoras,
                aumentaHoras,
                disminuyeHoras);
    }

    public void aplicarDatosEdicionAlEvento(
            Evento evento,
            EventoDTO dto,
            LocalDateTime inicio,
            LocalDateTime finAjustado,
            CambiosEdicionEvento cambios,
            int nuevaDuracion,
            double costoFinal) {
        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        evento.setFecha(inicio);
        evento.setFechaFin(finAjustado);
        evento.setUbicacion(cambios.ubicNueva());
        evento.setAforoMaximo(dto.getAforoMaximo());
        evento.setImagen(dto.getImagen());
        if (dto.getCategoria() != null) {
            evento.setCategoria(dto.getCategoria());
        }
        if (dto.getTipoEvento() != null) {
            evento.setTipoEvento(dto.getTipoEvento());
        }
        evento.setDuracionHoras(nuevaDuracion);
        evento.setCosto(costoFinal);
        evento.setMotivoRechazo(null);
    }

    private static String normalizarCategoria(String cat) {
        return cat == null || cat.isBlank() ? "" : cat.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean cambiaTexto(String actual, String nuevo) {
        String a = actual == null ? "" : actual.trim();
        String b = nuevo == null ? "" : nuevo.trim();
        return !a.equals(b);
    }
}
