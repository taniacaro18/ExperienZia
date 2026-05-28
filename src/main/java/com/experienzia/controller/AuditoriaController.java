package com.experienzia.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.experienzia.dto.AuditoriaDTO;
import com.experienzia.service.AuditoriaService;

// Solo admin debería usar esto: quién hizo qué y desde qué IP
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<AuditoriaDTO>> listarTodo() {
        return ResponseEntity.ok(auditoriaService.listarTodo());
    }

    // Historial de un usuario concreto (quién hizo qué)
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<AuditoriaDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(auditoriaService.listarPorUsuario(usuarioId));
    }

    // Filtrar por tipo de entidad: Evento, Usuario, Inscripcion...
    // Filtrar por tipo de entidad: Evento, Usuario, Inscripcion...
    @GetMapping("/entidad/{tipo}")
    public ResponseEntity<List<AuditoriaDTO>> listarPorEntidad(@PathVariable String tipo) {
        return ResponseEntity.ok(auditoriaService.listarPorEntidad(tipo));
    }
}
