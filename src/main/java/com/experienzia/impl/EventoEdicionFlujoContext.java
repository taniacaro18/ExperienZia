package com.experienzia.impl;

import com.experienzia.dto.EventoDTO;
import com.experienzia.entity.EstadoEvento;
import com.experienzia.entity.Evento;
import com.experienzia.entity.Pago;

import java.util.Map;
import java.util.Optional;

// Bolsa de datos que paso entre los helpers cuando el organizador edita un evento ya gestionado.
// Así no repito mil parámetros en cada método del flujo de edición.
public record EventoEdicionFlujoContext(
        Evento evento, // el que estoy editando en BD
        EventoDTO dto, // lo que mandó el organizador desde el front
        EstadoEvento estadoAntes, // para saber si ya estaba publicado y aplicar reglas duras
        int viejaDuracion,
        int nuevaDuracion,
        double viejoCosto,
        double costoFinal,
        CambiosEdicionEvento cambios, // flags de qué campos tocó
        Map<String, Object> snapshotAntes, // por si el admin rechaza y hay que revertir
        Optional<Pago> pagoOpt, // a veces no hay pago todavía, por eso Optional y no me vuelvo loco
        boolean pagoAprobado) {

    // Solo aplican reglas raras de pago/revisión si el evento ya estaba ACTIVO o APROBADO antes de tocarlo.
    public boolean gestionadoActivoOaprobado() {
        return estadoAntes == EstadoEvento.ACTIVO || estadoAntes == EstadoEvento.APROBADO;
    }
}
