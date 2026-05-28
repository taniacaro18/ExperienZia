package com.experienzia.impl;

// Flags que armo comparando el evento en BD vs lo que mandó el front en el PUT de edición.
// Me sirve para saber si toca revisión admin, suplemento de pago, etc.
public record CambiosEdicionEvento(
        String ubicNueva,
        boolean cambiaNombre,
        boolean cambiaDescripcion,
        boolean cambiaImagen,
        boolean cambiaUbicacion,
        boolean cambiaAforo,
        boolean cambiaTipo,
        boolean cambiaCategoria,
        boolean cambiaDuracion,
        boolean cambiaAgenda,
        boolean requiereRevisionTipoCat,
        boolean soloMetadatosSinTipoCatNiHoras,
        boolean aumentaHoras, // más horas = puede tocar suplemento de pago
        boolean disminuyeHoras) { // menos horas = penalización del 5% por hora

    // Si movió fechas, ubicación o duración, tengo que validar que el salón no choque con otro evento.
    public boolean requiereValidarUbicacion() {
        return cambiaAgenda || cambiaUbicacion || cambiaDuracion;
    }

    // Cambios "suaves" (nombre, foto…) que igual pueden ir a cola de revisión si el evento ya estaba publicado.
    public boolean requiereRevisionMetadatos() {
        return soloMetadatosSinTipoCatNiHoras
                && (cambiaNombre || cambiaDescripcion || cambiaImagen || cambiaUbicacion || cambiaAforo || cambiaAgenda);
    }
}
