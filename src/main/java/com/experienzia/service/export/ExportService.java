package com.experienzia.service.export;

import com.experienzia.dto.AsistenteEventoDTO;
import com.experienzia.dto.CertificadoDTO;
import com.experienzia.dto.EventoDTO;

import java.util.List;

/**
 * Interfaz para generar archivos de exportación en el servidor.
 * Crea Excel (.xlsx) y PDF con Apache POI y OpenPDF para reportes y listados.
 */
/**
 * Interfaz del servicio ExportService.
 * Define qué operaciones puede hacer el backend; la clase *Impl las programa.
 */
public interface ExportService {

    /** Genera un Excel con la lista de asistentes de un evento. */
    byte[] resumenAsistentesExcel(EventoDTO evento, List<AsistenteEventoDTO> asistentes);

    /** Genera un PDF con la lista de asistentes de un evento. */
    byte[] resumenAsistentesPdf(EventoDTO evento, List<AsistenteEventoDTO> asistentes);

    /** Genera un Excel con el listado de eventos. */
    byte[] eventosExcel(List<EventoDTO> eventos);

    /** Genera un PDF con el listado de eventos. */
    byte[] eventosPdf(List<EventoDTO> eventos);

    /** Genera el PDF del certificado de asistencia (nombre, evento, código, etc.). */
    byte[] certificadoPdf(CertificadoDTO certificado);
}
