package com.vidasalud.catalog.dto;

import com.vidasalud.catalog.model.Prestacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrestacionResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer availableSlots;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PrestacionResponse fromEntity(Prestacion prestacion) {
        return PrestacionResponse.builder()
                .id(prestacion.getId())
                .name(prestacion.getName())
                .description(prestacion.getDescription())
                .price(prestacion.getPrice())
                .availableSlots(prestacion.getAvailableSlots())
                .active(prestacion.getActive())
                .createdAt(prestacion.getCreatedAt())
                .updatedAt(prestacion.getUpdatedAt())
                .build();
    }
}
