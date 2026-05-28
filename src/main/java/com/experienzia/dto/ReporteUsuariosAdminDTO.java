package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Métricas de usuarios para reporte admin
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteUsuariosAdminDTO {
    private long usuariosTotales;
    private long usuariosActivos;
    private long usuariosPendientes;
    private long organizadoresActivos;
    private long asistentesRegistrados;
    private long staffActivo;
    private List<PuntoSerieDTO> crecimientoMensualUsuarios;
    private List<PuntoSerieDTO> crecimientoMensualEventos;
}
