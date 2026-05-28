package com.experienzia.service;

import com.experienzia.dto.AforoEnVivoDTO;
import com.experienzia.dto.AsistenteEventoDTO;
import com.experienzia.dto.EventoStaffDTO;
import com.experienzia.dto.FilaAsistenteCargaDTO;
import com.experienzia.dto.InscripcionDTO;
import com.experienzia.dto.ResultadoCargaAsistentesDTO;
import com.experienzia.dto.StaffAsignadoDTO;
import com.experienzia.entity.FuncionStaff;

import java.util.List;

// Inscripciones, check-in/out, carga masiva de asistentes y asignación de staff al evento
public interface InscripcionService {

    // Asistente se apunta a un evento (valido aforo y duplicados en la BD)
    InscripcionDTO inscribir(Long usuarioId, Long eventoId);

    // Cuando el evento queda ACTIVO meto al organizador como asistente; si ya está, no hago nada
    InscripcionDTO inscribirOrganizadorEnSuEvento(Long eventoId);

    // Cancela inscripción y bajo aforo si aplica
    InscripcionDTO cancelar(Long inscripcionId);

    // Staff hace check-in manual por id de inscripción
    InscripcionDTO checkIn(Long inscripcionId, Long staffUsuarioId);

    // Staff escanea QR en puerta (valido que sea de ese evento)
    InscripcionDTO checkInPorQR(String codigoQR, Long staffUsuarioId, Long eventoId);

    // Registro de salida manual
    InscripcionDTO checkOut(Long inscripcionId, Long staffUsuarioId);

    // Salida escaneando QR
    InscripcionDTO checkOutPorQR(String codigoQR, Long staffUsuarioId, Long eventoId);

    // Lista de inscripciones de un evento
    List<InscripcionDTO> listarPorEvento(Long eventoId);

    // Mis inscripciones como usuario
    List<InscripcionDTO> listarPorUsuario(Long usuarioId);

    // Panel staff: asistentes con búsqueda (valido que el staff esté asignado)
    List<AsistenteEventoDTO> listarAsistentesParaStaff(Long eventoId, Long staffUsuarioId, String busqueda);

    // Igual pero solo si quien consulta es el organizador del evento
    List<AsistenteEventoDTO> listarAsistentesParaOrganizador(Long eventoId, Long organizadorId, String busqueda);

    // Aforo en vivo para el front (dentro/fuera/cupo)
    AforoEnVivoDTO consultarAforoEnVivo(Long eventoId);

    // Carga manual fila por fila desde el front del organizador
    ResultadoCargaAsistentesDTO cargarAsistentesManual(Long eventoId, Long organizadorId, List<FilaAsistenteCargaDTO> filas);

    // Carga masiva por CSV (mismo flujo, otro formato)
    ResultadoCargaAsistentesDTO cargarAsistentesCsv(Long eventoId, Long organizadorId, String contenidoCsv);

    // Organizador asigna staff con función (QR, manual, salida, general)
    void asignarStaff(Long eventoId, Long organizadorId, Long staffUsuarioId, FuncionStaff funcion);

    // Cambio la función de un staff ya asignado
    StaffAsignadoDTO cambiarFuncionStaff(Long eventoId, Long organizadorId, Long staffUsuarioId, FuncionStaff funcion);

    // Quito staff del evento
    void desasignarStaff(Long eventoId, Long organizadorId, Long staffUsuarioId);

    // Solo ids (clientes viejos del front)
    List<Long> listarStaffIdsPorEvento(Long eventoId);

    // Lista con nombre y función para pintar en el panel
    List<StaffAsignadoDTO> listarStaffPorEvento(Long eventoId);

    // Eventos donde trabajo como staff (mi panel)
    List<EventoStaffDTO> listarEventosDelStaff(Long staffUsuarioId);
}
