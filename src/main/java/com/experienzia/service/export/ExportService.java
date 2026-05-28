package com.experienzia.service.export;

import com.experienzia.dto.AsistenteEventoDTO;
import com.experienzia.dto.CertificadoDTO;
import com.experienzia.dto.EventoDTO;
import com.experienzia.dto.ReportePagosAdminDTO;
import com.experienzia.dto.ReporteUsuariosAdminDTO;

import java.util.List;

// Genero archivos Excel/PDF en el servidor para que el front los descargue
public interface ExportService {

    // Excel con lista de asistentes de un evento
    byte[] resumenAsistentesExcel(EventoDTO evento, List<AsistenteEventoDTO> asistentes);

    // PDF con la misma lista de asistentes
    byte[] resumenAsistentesPdf(EventoDTO evento, List<AsistenteEventoDTO> asistentes);

    // Excel con listado de eventos
    byte[] eventosExcel(List<EventoDTO> eventos);

    // PDF con listado de eventos
    byte[] eventosPdf(List<EventoDTO> eventos);

    // PDF del certificado (nombre, evento, código...) para descargar
    byte[] certificadoPdf(CertificadoDTO certificado);

    // Reportes ejecutivos admin con logo y KPIs
    byte[] reportePagosAdminPdf(ReportePagosAdminDTO reporte);

    byte[] reporteUsuariosAdminPdf(ReporteUsuariosAdminDTO reporte);
}
