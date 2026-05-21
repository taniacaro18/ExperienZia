// Archivo `shared/estado.helpers.ts` — componente reutilizable: estado.helpers.
/**
 * Funciones auxiliares (helpers) para mostrar estados en la UI:
 * textos en español, colores de tags PrimeNG y barra de aforo.
 * No llaman a la API; solo transforman datos para la vista.
 */
import { EstadoEvento, EstadoInscripcion, EstadoPago, EstadoUsuario } from '../core/models/domain.models';

/** Tipo de color que usa PrimeNG en los tags (success, warn, etc.) */
export type Severity = 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast';

/** Devuelve el nombre legible del estado de un evento (ej. ACTIVO → "Activo") */
export function eventoEstadoLabel(e: EstadoEvento | string): string {
  switch (e) {
    case 'ACTIVO': return 'Activo';
    case 'APROBADO': return 'Pend. Pago';
    case 'PENDIENTE': return 'Pend. Aprobación';
    case 'RECHAZADO': return 'Rechazado';
    case 'CANCELADO': return 'Cancelado';
    case 'FINALIZADO': return 'Finalizado';
    case 'PENDIENTE_REVISION': return 'Pend. revisión cambios';
    case 'PENDIENTE_SUPLEMENTO': return 'Pend. pago adicional';
    case 'PENDIENTE_CANCELACION': return 'Pend. cancelación';
    default: return String(e);
  }
}

/** Elige el color del tag según el estado del evento */
export function eventoEstadoSeverity(e: EstadoEvento | string): Severity {
  switch (e) {
    case 'ACTIVO': return 'success';
    case 'APROBADO': return 'info';
    case 'PENDIENTE': return 'warn';
    case 'RECHAZADO':
    case 'CANCELADO': return 'danger';
    case 'FINALIZADO': return 'secondary';
    case 'PENDIENTE_REVISION': return 'warn';
    case 'PENDIENTE_SUPLEMENTO': return 'warn';
    case 'PENDIENTE_CANCELACION': return 'danger';
    default: return 'info';
  }
}

export function usuarioEstadoSeverity(e: EstadoUsuario | string): Severity {
  switch (e) {
    case 'ACTIVO': return 'success';
    case 'PENDIENTE': return 'warn';
    case 'INACTIVO':
    case 'RECHAZADO': return 'danger';
    default: return 'info';
  }
}

export function inscripcionEstadoSeverity(e: EstadoInscripcion | string): Severity {
  switch (e) {
    case 'ASISTIO': return 'success';
    case 'INSCRITO': return 'info';
    case 'CANCELADO': return 'secondary';
    default: return 'warn';
  }
}

export function pagoEstadoSeverity(e: EstadoPago | string): Severity {
  switch (e) {
    case 'APROBADO': return 'success';
    case 'PENDIENTE': return 'warn';
    case 'RECHAZADO': return 'danger';
    default: return 'info';
  }
}

export function porcentajeOcupacion(actual: number, maximo: number): number {
  if (!maximo || maximo <= 0) return 0;
  return Math.min(100, Math.round((actual / maximo) * 100));
}

export function colorOcupacion(p: number): string {
  if (p >= 90) return 'bg-coral-500';
  if (p >= 70) return 'bg-brand-500';
  if (p >= 40) return 'bg-brand-400';
  return 'bg-accent-500';
}

export function rolLabel(r: string): string {
  switch (r) {
    case 'ADMIN': return 'Administrador';
    case 'ORGANIZADOR': return 'Organizador';
    case 'ASISTENTE': return 'Asistente';
    case 'STAFF': return 'Staff';
    default: return r;
  }
}

export function rolSeverity(r: string): Severity {
  switch (r) {
    case 'ADMIN': return 'danger';
    case 'ORGANIZADOR': return 'info';
    case 'ASISTENTE': return 'success';
    case 'STAFF': return 'warn';
    default: return 'secondary';
  }
}

export function accionAuditoriaIcono(accion: string): { icon: string; tone: string } {
  const a = accion.toUpperCase();
  if (a.includes('APROBAD') || a.includes('REACTIVAD')) return { icon: 'pi-check-circle', tone: 'text-emerald-600 bg-emerald-100' };
  if (a.includes('RECHAZAD') || a.includes('DESACTIVAD') || a.includes('CANCELADO')) return { icon: 'pi-times-circle', tone: 'text-rose-600 bg-rose-100' };
  if (a.includes('CREADO') || a.includes('REGISTRA')) return { icon: 'pi-plus-circle', tone: 'text-brand-600 bg-brand-100' };
  if (a.includes('EDITAD') || a.includes('ACTUALIZAD') || a.includes('CAMBIAD')) return { icon: 'pi-pencil', tone: 'text-sky-600 bg-sky-100' };
  if (a.includes('CHECK_IN') || a.includes('CHECK-IN')) return { icon: 'pi-sign-in', tone: 'text-emerald-600 bg-emerald-100' };
  if (a.includes('CHECK_OUT') || a.includes('CHECK-OUT')) return { icon: 'pi-sign-out', tone: 'text-coral-600 bg-coral-100' };
  if (a.includes('PAGO')) return { icon: 'pi-wallet', tone: 'text-brand-700 bg-brand-100' };
  if (a.includes('CREDENCIA')) return { icon: 'pi-key', tone: 'text-amber-600 bg-amber-100' };
  return { icon: 'pi-history', tone: 'text-surface-700 bg-surface-100' };
}
