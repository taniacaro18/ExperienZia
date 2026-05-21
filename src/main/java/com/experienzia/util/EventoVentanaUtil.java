package com.experienzia.util;

import com.experienzia.entity.Evento;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utilidades para calcular cuándo empieza/termina un evento
 * y si dos eventos se pisan en el tiempo (solapamiento).
 */
public final class EventoVentanaUtil {

	// no instanciamos, solo métodos static
	private EventoVentanaUtil() {
	}

	/**
	 * Convierte el string de zona horaria de config a ZoneId.
	 * Si falla, usa la zona del sistema.
	 */
	public static ZoneId zoneId(String zonaConfig) {
		String z = zonaConfig != null ? zonaConfig.trim() : "America/Bogota";
		try {
			return ZoneId.of(z);
		} catch (Exception ex) {
			return ZoneId.systemDefault();
		}
	}

	/**
	 * Fin de la ventana del evento: usa fechaFin si está bien;
	 * si no, inicio + duracionHoras (mínimo 1 hora).
	 */
	public static LocalDateTime instanteFin(Evento e) {
		LocalDateTime inicio = e.getFecha();
		LocalDateTime fin = e.getFechaFin();
		if (fin != null && !fin.isBefore(inicio)) {
			return fin;
		}
		int horas = e.getDuracionHoras() != null && e.getDuracionHoras() > 0 ? e.getDuracionHoras() : 1;
		return inicio.plusHours(horas);
	}

	/**
	 * Mismo instanteFin pero con zona horaria (para comparar con "ahora").
	 */
	public static ZonedDateTime instanteFinZoned(Evento e, ZoneId zone) {
		return instanteFin(e).atZone(zone);
	}

	/**
	 * Inicio del evento en la zona indicada.
	 */
	public static ZonedDateTime instanteInicioZoned(Evento e, ZoneId zone) {
		return e.getFecha().atZone(zone);
	}

	/**
	 * Dice si dos ventanas de tiempo se solapan (intervalo [inicio, fin) ).
	 */
	public static boolean ventanasSeSolapan(LocalDateTime inicio1, LocalDateTime fin1, Evento otro) {
		LocalDateTime inicio2 = otro.getFecha();
		LocalDateTime fin2 = instanteFin(otro);
		return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
	}

	/**
	 * true si el evento ya terminó según la hora actual en esa zona.
	 */
	public static boolean eventoYaFinalizo(Evento e, ZoneId zone) {
		return !instanteFinZoned(e, zone).isAfter(ZonedDateTime.now(zone));
	}
}
