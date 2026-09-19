package com.vidasalud.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoxRequest {

    @NotBlank(message = "name es obligatorio")
    private String name;

    private String location;

    @NotNull(message = "totalCapacity es obligatorio")
    @Min(value = 1, message = "totalCapacity debe ser al menos 1")
    private Integer totalCapacity;
}
