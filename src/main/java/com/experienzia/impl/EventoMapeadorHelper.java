package com.experienzia.impl;

import com.experienzia.dto.EventoDTO;
import com.experienzia.entity.Evento;
import com.experienzia.entity.Usuario;
import com.experienzia.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

// Convierto Evento → DTO para no repetir el mismo map en todos los servicios.
@Component
public class EventoMapeadorHelper {

    private final ModelMapper modelMapper;
    private final UsuarioRepository usuarioRepository;

    public EventoMapeadorHelper(ModelMapper modelMapper, UsuarioRepository usuarioRepository) {
        this.modelMapper = modelMapper;
        this.usuarioRepository = usuarioRepository;
    }

    public EventoDTO toDto(Evento evento) {
        EventoDTO dto = modelMapper.map(evento, EventoDTO.class);
        // Me aseguro de que PUBLICO/PRIVADO siempre viaje al front en tablas y catálogo interno
        dto.setTipoEvento(evento.getTipoEvento());
        // A veces el organizador no viene cargado en la entidad; lo busco por ID para mostrar nombre en el front.
        Usuario org = evento.getOrganizador();
        if (org == null && evento.getOrganizadorId() != null) {
            org = usuarioRepository.findById(evento.getOrganizadorId()).orElse(null);
        }
        if (org != null) {
            dto.setOrganizadorNombre(org.getNombre());
            dto.setOrganizadorEmail(org.getEmail());
        }
        return dto;
    }

    // Vista pública del catálogo: oculto costo, aforo y datos del organizador (no es panel interno).
    public EventoDTO toCatalogoPublicoDto(Evento evento) {
        EventoDTO dto = toDto(evento);
        dto.setCosto(null);
        dto.setAforoMaximo(null);
        dto.setAforoActual(null);
        dto.setOrganizadorId(null);
        dto.setOrganizadorNombre(null);
        dto.setOrganizadorEmail(null);
        return dto;
    }

    // Mensaje que el front muestra como banner cuando la edición quedó en revisión o hay que pagar más.
    public EventoDTO conAlerta(Evento evento, String alerta) {
        EventoDTO dto = toDto(evento);
        dto.setAlertaNegocio(alerta);
        return dto;
    }
}
