package com.experienzia.impl;

import com.experienzia.dto.AuditoriaDTO;
import com.experienzia.entity.Auditoria;
import com.experienzia.repository.AuditoriaRepository;
import com.experienzia.service.AuditoriaService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
/**
 * Clase de implementación del módulo Auditoria.
 * Aquí va la lógica de negocio (validar, guardar en BD, etc.).
 */
public class AuditoriaServiceImpl implements AuditoriaService {

    /** Dato del campo auditoria repository */
    private final AuditoriaRepository auditoriaRepository;
    /** Dato del campo model mapper */
    private final ModelMapper modelMapper;

    public AuditoriaServiceImpl(AuditoriaRepository auditoriaRepository, ModelMapper modelMapper) {
        this.auditoriaRepository = auditoriaRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    /** Ejecuta `registrar` (lógica del servicio). */
    public AuditoriaDTO registrar(Long usuarioId, String accion, String entidad, Long entidadId, String direccionIp) {
        if (accion == null || accion.trim().isEmpty()) {
            throw new IllegalArgumentException("La acción de auditoría no puede estar vacía.");
        }
        if (entidad == null || entidad.trim().isEmpty()) {
            throw new IllegalArgumentException("La entidad de auditoría no puede estar vacía.");
        }
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuarioId(usuarioId);
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setEntidadId(entidadId);
        auditoria.setFecha(LocalDateTime.now());
        auditoria.setDireccionIp(direccionIp);
        return modelMapper.map(auditoriaRepository.save(auditoria), AuditoriaDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    /** Ejecuta `listarTodo` (lógica del servicio). */
    public List<AuditoriaDTO> listarTodo() {
        return auditoriaRepository.findAllByOrderByFechaDesc().stream()
                .map(a -> modelMapper.map(a, AuditoriaDTO.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    /** Ejecuta `listarPorUsuario` (lógica del servicio). */
    public List<AuditoriaDTO> listarPorUsuario(Long usuarioId) {
        return auditoriaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(a -> modelMapper.map(a, AuditoriaDTO.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    /** Ejecuta `listarPorEntidad` (lógica del servicio). */
    public List<AuditoriaDTO> listarPorEntidad(String entidad) {
        return auditoriaRepository.findByEntidadOrderByFechaDesc(entidad).stream()
                .map(a -> modelMapper.map(a, AuditoriaDTO.class))
                .toList();
    }
}
