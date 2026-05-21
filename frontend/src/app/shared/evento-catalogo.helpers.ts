// Archivo `shared/evento-catalogo.helpers.ts` — componente reutilizable: evento catalogo.helpers.
/**
 * Helpers de fechas para el catálogo de eventos.
 * Calculan si un evento sigue vigente según el reloj del navegador (sin API).
 */
import { Evento } from '../core/models/domain.models';

/** Calcula en milisegundos cuándo termina el evento (fechaFin o inicio + duración) */
export function instanteFinEventoMs(e: Evento): number {
  const inicioMs = new Date(e.fecha).getTime();
  if (e.fechaFin) {
    const finMs = new Date(e.fechaFin).getTime();
    if (finMs >= inicioMs) {
      return finMs;
    }
  }
  const h = e.duracionHoras != null && e.duracionHoras > 0 ? e.duracionHoras : 1;
  return inicioMs + h * 3_600_000;
}

/** Refuerzo en UI: el catálogo público no debe mostrar eventos cuya ventana ya pasó (según el reloj del navegador). */
export function eventoSigueVigenteEnCatalogoPublico(e: Evento): boolean {
  return instanteFinEventoMs(e) > Date.now();
}

/** true si ya pasó la hora de fin (fechaFin o inicio + duración), según el reloj del navegador. */
export function eventoVentanaYaCerro(e: Evento): boolean {
  return instanteFinEventoMs(e) <= Date.now();
}
