package com.experienzia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Body del rechazo de evento pendiente — solo necesito el motivo para guardarlo en BD.
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RechazarEventoDTO {

    private String motivo;
}
