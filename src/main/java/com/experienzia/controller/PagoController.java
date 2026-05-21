package com.experienzia.controller;

import com.experienzia.dto.PagoDTO;
import com.experienzia.dto.RechazarPagoDTO;
import com.experienzia.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.experienzia.util.ClientIpResolver;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de pagos. Solo el ORGANIZADOR registra pagos para sus eventos;
 * el ADMIN aprueba/rechaza. Asistentes y staff no interactúan con este recurso.
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    /** Registra el pago de la tarifa de un evento. */
    // POST /api/pagos — sube el comprobante de pago de un evento
    @PostMapping
    public ResponseEntity<PagoDTO> registrar(
            @RequestParam Long eventoId,
            @RequestParam Long organizadorId,
            // MultipartFile es el archivo que envía el cliente (imagen o PDF del comprobante)
            @RequestParam MultipartFile archivo,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagoService.registrar(eventoId, organizadorId, archivo, ClientIpResolver.resolve(request)));
    }

    // PUT /api/pagos/{id}/aprobar — el admin aprueba un pago pendiente
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<PagoDTO> aprobar(
            @PathVariable Long id,
            @RequestParam(required = false) Long aprobadorId,
            HttpServletRequest request) {
        return ResponseEntity.ok(pagoService.aprobar(id, aprobadorId, ClientIpResolver.resolve(request)));
    }

    // PUT /api/pagos/{id}/rechazar — el admin rechaza el pago (motivo en el cuerpo JSON)
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<PagoDTO> rechazar(
            @PathVariable Long id,
            @RequestBody RechazarPagoDTO body,
            HttpServletRequest request) {
        return ResponseEntity.ok(pagoService.rechazar(id,
                body == null ? null : body.getMotivo(),
                body == null ? null : body.getAprobadorId(),
                ClientIpResolver.resolve(request)));
    }

    // GET /api/pagos/pendientes — lista pagos que aún no decidió el admin
    @GetMapping("/pendientes")
    public ResponseEntity<List<PagoDTO>> listarPendientes() {
        return ResponseEntity.ok(pagoService.listarPendientes());
    }

    /** HU-021: historial completo con quién aprobó/rechazó y la fecha de la decisión. */
    // GET /api/pagos — historial de todos los pagos
    @GetMapping
    public ResponseEntity<List<PagoDTO>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    /** Historial de pagos de un organizador específico. */
    // GET /api/pagos/organizador/{organizadorId}
    @GetMapping("/organizador/{organizadorId}")
    public ResponseEntity<List<PagoDTO>> listarPorOrganizador(@PathVariable Long organizadorId) {
        return ResponseEntity.ok(pagoService.listarPorOrganizador(organizadorId));
    }

    /** Pago asociado a un evento (comprobante, complemento, estado). */
    // GET /api/pagos/evento/{eventoId} — devuelve 404 si ese evento no tiene pago
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<PagoDTO> obtenerPorEvento(@PathVariable Long eventoId) {
        Optional<PagoDTO> dto = pagoService.obtenerPorEvento(eventoId);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
