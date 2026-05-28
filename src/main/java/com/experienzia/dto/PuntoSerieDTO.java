package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Par periodo-valor que uso en las series de los dashboards (Chart.js en el front).
@Data
@NoArgsConstructor
@AllArgsConstructor

public class PuntoSerieDTO {

    private String periodo;
    
    private long valor;
}
