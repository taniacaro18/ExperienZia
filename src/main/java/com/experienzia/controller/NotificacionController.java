package com.experienzia.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.experienzia.dto.NotificacionDTO;
import com.experienzia.service.NotificacionService;

// Campanita del front: listar y marcar leídas
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    // Campanita del front: todas las notis de ese usuario
    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<NotificacionDTO>> listar(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(notificacionService.listarPorUsuario(usuarioId));
    }

    // Cuando el usuario abre una noti en el front, la marco leída acá
    @PutMapping("/{id}/leida")
    public ResponseEntity<NotificacionDTO> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarLeida(id));
    }
}
