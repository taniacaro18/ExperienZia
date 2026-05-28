package com.experienzia.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experienzia.dto.CertificadoDTO;
import com.experienzia.service.CertificadoService;
import com.experienzia.service.export.ExportService;

// Certificados de asistencia: generar, validar por código público, bajar PDF
@RestController
@RequestMapping("/api/certificados")
public class CertificadoController {

    private final CertificadoService certificadoService;
    private final ExportService exportService;

    public CertificadoController(CertificadoService certificadoService, ExportService exportService) {
        this.certificadoService = certificadoService;
        this.exportService = exportService;
    }

    // Un certificado por inscripción con check-in hecho
    @PostMapping("/generar/{inscripcionId}")
    public ResponseEntity<CertificadoDTO> generar(
            @PathVariable Long inscripcionId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(certificadoService.generar(inscripcionId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CertificadoDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(certificadoService.listarPorUsuario(usuarioId));
    }

    // Ruta pública — cualquiera con el código puede validar sin login
    @GetMapping("/validar/{codigo}")
    public ResponseEntity<CertificadoDTO> validar(@PathVariable String codigo) {
        return ResponseEntity.ok(certificadoService.validarPorCodigo(codigo));
    }

    @GetMapping(value = "/pdf/{codigo}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdfPorCodigo(@PathVariable String codigo) {
        CertificadoDTO dto = certificadoService.validarPorCodigo(codigo);
        byte[] pdf = exportService.certificadoPdf(dto);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificado_" + codigo + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    // Organizador genera certificados de todos los que hicieron check-in
    @PostMapping("/evento/{eventoId}/generar-masivo")
    public ResponseEntity<List<CertificadoDTO>> generarMasivo(
            @PathVariable Long eventoId,
            @RequestParam(required = false) Long organizadorId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(certificadoService.generarMasivoPorEvento(eventoId, organizadorId));
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<CertificadoDTO>> listarPorEvento(@PathVariable Long eventoId) {
        return ResponseEntity.ok(certificadoService.listarPorEvento(eventoId));
    }
}
