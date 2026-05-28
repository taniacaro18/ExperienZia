package com.experienzia.impl;

import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.Evento;
import com.experienzia.exceptions.CustomException;
import com.experienzia.util.EventoVentanaUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Freno check-in/out si el evento no está ACTIVO o si estamos fuera de la ventana horaria del evento.
@Component
public class InscripcionValidator {

    // Formato bonito para los mensajes de error al staff (fechas en español Colombia).
    public static final DateTimeFormatter FMT_VENTANA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CO"));

    @Value("${experienzia.eventos.zona-horaria:America/Bogota}")
    private String zonaHorariaEventos;

    // El staff solo puede marcar entrada/salida mientras el evento "está en curso" según fechas y zona Colombia.
    public void validarEventoActivoYEnFecha(Evento evento, String accion) {
        if (evento.getEstado() != EstadoEvento.ACTIVO) {
            throw new CustomException(
                    "El evento no está activo. Solo se puede " + accion + " en eventos activos.",
                    HttpStatus.BAD_REQUEST);
        }
        if (evento.getFecha() == null) {
            throw new CustomException("El evento no tiene fecha definida.", HttpStatus.BAD_REQUEST);
        }
        // Comparo "ahora" vs inicio/fin en la misma zona; si no, las fechas de la BD no cuadran con el reloj real.
        ZoneId zone = EventoVentanaUtil.zoneId(zonaHorariaEventos);
        ZonedDateTime ahora = ZonedDateTime.now(zone);
        ZonedDateTime inicio = EventoVentanaUtil.instanteInicioZoned(evento, zone);
        ZonedDateTime fin = EventoVentanaUtil.instanteFinZoned(evento, zone);
        if (ahora.isBefore(inicio)) {
            throw new CustomException(
                    "Solo se puede " + accion + " a partir del inicio del evento ("
                            + inicio.format(FMT_VENTANA)
                            + "). Aún no ha comenzado la ventana horaria.",
                    HttpStatus.BAD_REQUEST);
        }
        if (ahora.isAfter(fin)) {
            throw new CustomException(
                    "No se puede " + accion + ": el evento ya finalizó. La ventana terminó el "
                            + fin.format(FMT_VENTANA)
                            + ".",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
