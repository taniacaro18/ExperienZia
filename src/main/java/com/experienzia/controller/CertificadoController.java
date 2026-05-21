package com.experienzia.controller;

import com.experienzia.dto.CertificadoDTO;
import com.experienzia.service.CertificadoService;
import com.experienzia.service.export.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController le dice a Spring que esta clase responde peticiones HTTP (API REST)
@RestController
// @RequestMapping pone el prefijo de URL para todos los métodos de esta clase
@RequestMapping("/api/certificados")
public class CertificadoController {

    // Aquí guardamos los servicios que hacen el trabajo real (base de datos, reglas, etc.)
    private final CertificadoService certificadoService;
    private final ExportService exportService;

    // El constructor recibe los servicios por inyección de dependencias de Spring
    public CertificadoController(CertificadoService certificadoService, ExportService exportService) {
        this.certificadoService = certificadoService;
        this.exportService = exportService;
    }

    // POST /api/certificados/generar/{inscripcionId} — crea un certificado para una inscripción
    @PostMapping("/generar/{inscripcionId}")
    public ResponseEntity<CertificadoDTO> generar(
            // @PathVariable saca el id de la URL (ej: .../generar/5 → inscripcionId = 5)
            @PathVariable Long inscripcionId) {
        // Llamamos al servicio porque ahí está la lógica de negocio; el controlador solo recibe y responde
        return ResponseEntity.status(HttpStatus.CREATED).body(certificadoService.generar(inscripcionId));
    }

    // GET /api/certificados/usuario/{usuarioId} — lista los certificados de un usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CertificadoDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(certificadoService.listarPorUsuario(usuarioId));
    }

    // GET /api/certificados/validar/{codigo} — comprueba si un certificado es válido por su código
    @GetMapping("/validar/{codigo}")
    public ResponseEntity<CertificadoDTO> validar(@PathVariable String codigo) {
        return ResponseEntity.ok(certificadoService.validarPorCodigo(codigo));
    }

    /**
     * PDF del certificado generado en servidor (OpenPDF). Misma validez que {@link #validar(String)}.
     * Público: quien tenga el código puede descargarlo (acceso típico desde la web de verificación).
     */
    // GET /api/certificados/pdf/{codigo} — devuelve un archivo PDF (no JSON)
    // produces indica que el tipo de respuesta es application/pdf
    @GetMapping(value = "/pdf/{codigo}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdfPorCodigo(@PathVariable String codigo) {
        // Primero validamos el certificado y luego generamos el PDF con los datos
        CertificadoDTO dto = certificadoService.validarPorCodigo(codigo);
        byte[] pdf = exportService.certificadoPdf(dto);
        // ResponseEntity.ok() arma la respuesta HTTP con cabeceras para descargar el archivo
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificado_" + codigo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    /** HU-024: generación masiva de certificados para todos los asistentes confirmados de un evento. */
    // POST /api/certificados/evento/{eventoId}/generar-masivo — genera muchos certificados de una vez
    @PostMapping("/evento/{eventoId}/generar-masivo")
    public ResponseEntity<List<CertificadoDTO>> generarMasivo(
            @PathVariable Long eventoId,
            // @RequestParam lee un parámetro de la query (?organizadorId=1); required=false = opcional
            @RequestParam(required = false) Long organizadorId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(certificadoService.generarMasivoPorEvento(eventoId, organizadorId));
    }

    // GET /api/certificados/evento/{eventoId} — lista certificados de un evento concreto
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<CertificadoDTO>> listarPorEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(certificadoService.listarPorEvento(eventoId));
    }
}
