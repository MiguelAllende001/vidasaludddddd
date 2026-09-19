package com.vidasalud.catalog.dto;

import com.vidasalud.catalog.model.Box;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoxResponse {

    private Long id;
    private String name;
    private String location;
    private Integer totalCapacity;
    private Integer availableCapacity;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BoxResponse fromEntity(Box box) {
        return BoxResponse.builder()
                .id(box.getId())
                .name(box.getName())
                .location(box.getLocation())
                .totalCapacity(box.getTotalCapacity())
                .availableCapacity(box.getAvailableCapacity())
                .active(box.getActive())
                .createdAt(box.getCreatedAt())
                .updatedAt(box.getUpdatedAt())
                .build();
    }
}
