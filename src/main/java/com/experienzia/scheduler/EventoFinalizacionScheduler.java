package com.experienzia.scheduler;

import com.experienzia.service.EventoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Cada 10 min paso eventos ACTIVO → FINALIZADO si ya se acabó la fecha
@Component
public class EventoFinalizacionScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(EventoFinalizacionScheduler.class);

	private final EventoService eventoService;

	public EventoFinalizacionScheduler(EventoService eventoService) {
		this.eventoService = eventoService;
	}

	@Scheduled(cron = "0 */10 * * * *")
	public void finalizarEventosPasados() {
		try {
			eventoService.marcarEventosActivosFinalizados();
		} catch (Exception ex) {
			// No reviento el cron si la BD falla un rato
			LOG.warn("Marcar eventos finalizados: {}", ex.getMessage());
		}
	}
}
