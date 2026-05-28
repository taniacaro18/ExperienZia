package com.experienzia.service;

import com.experienzia.dto.DisponibilidadSalonDTO;
import com.experienzia.dto.EventoDTO;
import com.experienzia.dto.EventoNovedadDTO;
import com.experienzia.spec.EventoSpecification.EventoSearchCriteria;

import java.time.LocalDateTime;
import java.util.List;

// Todo el flujo de eventos: catálogo, organizador, admin y lo que el front pide contra la BD
public interface EventoService {

    // El organizador crea un evento nuevo (queda PENDIENTE hasta que pague y el admin apruebe)
    EventoDTO crear(EventoDTO dto);

    // Cambio datos del evento; si toca cosas sensibles puede quedar novedad PENDIENTE para el admin
    EventoDTO editar(Long id, EventoDTO dto);

    // Admin aprueba el evento para que pueda activarse con el pago
    EventoDTO aprobar(Long id);

    // Admin rechaza con motivo; el front muestra el mensaje al organizador
    EventoDTO rechazar(Long id, String motivo);

    // El organizador pide cancelar; no borro de golpe, queda solicitud en la BD
    EventoDTO cancelar(Long id, Long organizadorId, String motivo);

    // Admin confirma la cancelación que pidió el organizador
    EventoDTO aprobarCancelacion(Long id);

    // Admin dice que no se cancela y vuelvo el estado que tenía antes
    EventoDTO rechazarCancelacion(Long id, String motivo);

    // Cuando el pago de plataforma queda OK activo el evento en la BD
    EventoDTO activarPorPago(Long id);

    // Tras aprobar el comprobante del suplemento por horas extra reactivo el evento
    EventoDTO activarTrasSuplementoPago(Long eventoId);

    // Complemento de pago con el evento ya ACTIVO: actualizo saldo sin cambiar estado
    void resolverComplementoPagoSobreEventoActivo(Long eventoId);

    // Detalle de un evento por id (panel interno)
    EventoDTO obtenerPorId(Long id);

    // Listado general (admin u otros roles según el controller)
    List<EventoDTO> listarTodos();

    // Solo públicos ACTIVOS para el catálogo del front
    List<EventoDTO> listarCatalogoPublicoActivo();

    // Detalle del catálogo: público, activo y que aún no haya terminado la ventana horaria
    EventoDTO obtenerParaCatalogoPublico(Long id);

    // Mis eventos del organizador en su panel
    List<EventoDTO> listarPorOrganizador(Long organizadorId);

    // Búsqueda con filtros (tipo, estado, fechas...) que manda el front
    List<EventoDTO> buscar(EventoSearchCriteria criteria);

    // Historial de novedades (edición, cancelación, suplemento) de un evento
    List<EventoNovedadDTO> listarNovedades(Long eventoId);

    // Subo aforo actual cuando alguien se inscribe o hace check-in
    void aumentarAforo(Long eventoId);

    // Bajo aforo cuando cancelan o hacen check-out
    void disminuirAforo(Long eventoId);

    // Tarea automática: paso a FINALIZADO los ACTIVO cuya fecha/hora de fin ya pasó
    void marcarEventosActivosFinalizados();

    // Calendario de salón: veo ocupación y si la franja propuesta choca con otro evento
    DisponibilidadSalonDTO consultarDisponibilidadSalon(
            String ubicacion,
            LocalDateTime desde,
            LocalDateTime hasta,
            Long excluirEventoId,
            LocalDateTime propuestaInicio,
            LocalDateTime propuestaFin);
}
