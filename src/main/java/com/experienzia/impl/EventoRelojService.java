package com.experienzia.impl;

import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.Evento;
import com.experienzia.repository.EventoRepository;
import com.experienzia.util.EventoVentanaUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// Fechas del evento en zona Colombia y pasar ACTIVO → FINALIZADO cuando ya pasó la ventana.
@Service
public class EventoRelojService {

    private final EventoRepository eventoRepository;

    // Por defecto Bogotá; si ponen mal el ID en properties caigo al systemDefault sin tumbar el server.
    @Value("${experienzia.eventos.zona-horaria:America/Bogota}")
    private String zonaHorariaEventos;

    public EventoRelojService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public int calcularDuracionHoras(LocalDateTime inicio, LocalDateTime fin) {
        long minutos = Duration.between(inicio, fin).toMinutes();
        if (minutos <= 0) {
            return 1;
        }
        // Cualquier fracción de hora cuenta como hora entera para cobrar (90 min = 2 h).
        return (int) Math.ceil(minutos / 60.0);
    }

    // Job o llamada manual: si ya pasó la ventana, guardo FINALIZADO en la BD.
    @Transactional
    public void marcarEventosActivosFinalizados() {
        for (Evento e : eventoRepository.findByEstado(EstadoEvento.ACTIVO)) {
            if (eventoHaFinalizadoSuVentana(e)) {
                e.setEstado(EstadoEvento.FINALIZADO);
                eventoRepository.save(e);
            }
        }
    }

    // Delego en EventoVentanaUtil porque ahí está la lógica de fechaFin vs duracionHoras.
    public LocalDateTime instanteFinEvento(Evento e) {
        return EventoVentanaUtil.instanteFin(e);
    }

    public ZoneId zoneIdParaEventos() {
        String z = zonaHorariaEventos != null ? zonaHorariaEventos.trim() : "America/Bogota";
        try {
            return ZoneId.of(z);
        } catch (Exception ex) {
            return ZoneId.systemDefault();
        }
    }

    public ZonedDateTime instanteFinEnZona(Evento e) {
        return instanteFinEvento(e).atZone(zoneIdParaEventos());
    }

    public ZonedDateTime ahoraEnZonaEventos() {
        return ZonedDateTime.now(zoneIdParaEventos());
    }

    // Comparo fin del evento vs "ahora" en la misma zona — así el catálogo no muestra cosas raras.
    public boolean eventoHaFinalizadoSuVentana(Evento e) {
        return !instanteFinEnZona(e).isAfter(ahoraEnZonaEventos());
    }
}
