package com.vidasalud.catalog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CapacityAdjustmentRequest {

    // Positivo para liberar cupo (ej: al cancelar), negativo para
    // descontar cupo (ej: al confirmar una atención).
    @NotNull(message = "delta es obligatorio")
    private Integer delta;
}
