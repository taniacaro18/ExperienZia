package com.experienzia.controller;

import com.experienzia.dto.AuditoriaDTO;
import com.experienzia.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Esta clase expone la API para consultar el registro de auditoría (quién hizo qué)
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    // GET /api/auditoria — devuelve todo el historial de auditoría del sistema
    @GetMapping
    public ResponseEntity<List<AuditoriaDTO>> listarTodo() {
        return ResponseEntity.ok(auditoriaService.listarTodo());
    }

    // GET /api/auditoria/usuario/{usuarioId} — filtra por el usuario que hizo las acciones
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<AuditoriaDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(auditoriaService.listarPorUsuario(usuarioId));
    }

    // GET /api/auditoria/entidad/{tipo} — filtra por tipo de entidad (ej: "Evento", "Inscripcion")
    @GetMapping("/entidad/{tipo}")
    public ResponseEntity<List<AuditoriaDTO>> listarPorEntidad(@PathVariable String tipo) {
        return ResponseEntity.ok(auditoriaService.listarPorEntidad(tipo));
    }
}
