package com.vidasalud.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrestacionRequest {

    @NotBlank(message = "name es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "price es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "price no puede ser negativo")
    private BigDecimal price;

    @NotNull(message = "availableSlots es obligatorio")
    @Min(value = 0, message = "availableSlots no puede ser negativo")
    private Integer availableSlots;
}
