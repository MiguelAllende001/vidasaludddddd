package com.vidasalud.catalog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "BOXES")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "LOCATION")
    private String location;

    // Capacidad total de cupos del box
    @Column(name = "TOTAL_CAPACITY", nullable = false)
    private Integer totalCapacity;

    // Cupos que quedan disponibles ahora mismo.
    // Regla del caso: baja al confirmar una atención.
    @Column(name = "AVAILABLE_CAPACITY", nullable = false)
    private Integer availableCapacity;

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.active == null) {
            this.active = true;
        }
        if (this.availableCapacity == null) {
            this.availableCapacity = this.totalCapacity;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
