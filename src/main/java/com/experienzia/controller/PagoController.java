package com.experienzia.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.experienzia.dto.PagoDTO;
import com.experienzia.dto.RechazarPagoDTO;
import com.experienzia.service.PagoService;
import com.experienzia.util.ClientIpResolver;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoDTO> registrar(
            @RequestParam Long eventoId,
            @RequestParam Long organizadorId,
            @RequestParam MultipartFile archivo,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagoService.registrar(eventoId, organizadorId, archivo, ClientIpResolver.resolve(request)));
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<PagoDTO> aprobar(
            @PathVariable Long id,
            @RequestParam(required = false) Long aprobadorId,
            HttpServletRequest request) {
        return ResponseEntity.ok(pagoService.aprobar(id, aprobadorId, ClientIpResolver.resolve(request)));
    }

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

    @GetMapping("/pendientes")
    public ResponseEntity<List<PagoDTO>> listarPendientes() {
        return ResponseEntity.ok(pagoService.listarPendientes());
    }

    @GetMapping
    public ResponseEntity<List<PagoDTO>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }


    @GetMapping("/organizador/{organizadorId}")
    public ResponseEntity<List<PagoDTO>> listarPorOrganizador(@PathVariable Long organizadorId) {
        return ResponseEntity.ok(pagoService.listarPorOrganizador(organizadorId));
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<PagoDTO> obtenerPorEvento(@PathVariable Long eventoId) {
        Optional<PagoDTO> dto = pagoService.obtenerPorEvento(eventoId);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
