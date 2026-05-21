package com.experienzia.scheduler;

import com.experienzia.service.EventoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea automática que marca eventos como FINALIZADO
 * cuando ya pasó su fecha/hora de fin y seguían en ACTIVO.
 */
@Component
public class EventoFinalizacionScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(EventoFinalizacionScheduler.class);

	private final EventoService eventoService;

	public EventoFinalizacionScheduler(EventoService eventoService) {
		this.eventoService = eventoService;
	}

	/**
	 * Corre cada 10 minutos (minuto 0, 10, 20, 30, 40, 50 de cada hora).
	 * La expresión cron tiene 6 campos porque Spring usa segundos también.
	 */
	@Scheduled(cron = "0 */10 * * * *")
	public void finalizarEventosPasados() {
		try {
			eventoService.marcarEventosActivosFinalizados();
		} catch (Exception ex) {
			// no tiramos la excepción hacia arriba para que el scheduler siga vivo
			LOG.warn("Marcar eventos finalizados: {}", ex.getMessage());
		}
	}
}
