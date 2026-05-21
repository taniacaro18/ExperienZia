// Mapa de "de dónde volver" al salir del detalle de evento (/eventos/:id)
// Solo usamos estas claves en la URL para no redirigir a sitios raros
export const RETORNO_EVENTO_DETALLE: Record<string, { path: string; label: string }> = {
  'mis-inscripciones': { path: '/mis-inscripciones', label: 'Volver a mis inscripciones' },
  eventos: { path: '/eventos', label: 'Volver al catálogo' },
  inicio: { path: '/inicio', label: 'Volver al inicio' },
  'organizador-eventos': { path: '/organizador/eventos', label: 'Volver a mis eventos' },
  'admin-eventos': { path: '/admin/eventos', label: 'Volver a administración de eventos' }
};

// Dado un ?retorno= en la URL, devuelve path y texto del botón "Volver"
export function destinoRetornoEventoDetalle(
  key: string | null | undefined
): { path: string; label: string } | null {
  if (!key) return null;
  return RETORNO_EVENTO_DETALLE[key] ?? null;
}
