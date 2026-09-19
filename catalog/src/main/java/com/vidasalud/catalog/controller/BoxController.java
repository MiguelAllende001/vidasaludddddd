package com.vidasalud.catalog.controller;

import com.vidasalud.catalog.dto.BoxRequest;
import com.vidasalud.catalog.dto.BoxResponse;
import com.vidasalud.catalog.dto.CapacityAdjustmentRequest;
import com.vidasalud.catalog.model.Box;
import com.vidasalud.catalog.service.BoxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/boxes")
@RequiredArgsConstructor
public class BoxController {

    private final BoxService service;

    @GetMapping
    public ResponseEntity<List<BoxResponse>> findAll() {
        List<BoxResponse> results = service.findAll().stream()
                .map(BoxResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoxResponse> getById(@PathVariable Long id) {
        Box box = service.getById(id);
        return ResponseEntity.ok(BoxResponse.fromEntity(box));
    }

    @PostMapping
    public ResponseEntity<BoxResponse> create(@Valid @RequestBody BoxRequest request) {
        Box created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BoxResponse.fromEntity(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoxResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BoxRequest request) {
        Box updated = service.update(id, request);
        return ResponseEntity.ok(BoxResponse.fromEntity(updated));
    }

    // Este es el endpoint que, más adelante, llamará ms-vidasalud-appointments
    // (directo o vía RabbitMQ/notify) cuando una atención se CONFIRME o CANCELE,
    // para descontar o liberar cupo del box.
    @PutMapping("/{id}/capacity")
    public ResponseEntity<BoxResponse> adjustCapacity(
            @PathVariable Long id,
            @Valid @RequestBody CapacityAdjustmentRequest request) {
        Box updated = service.adjustCapacity(id, request.getDelta());
        return ResponseEntity.ok(BoxResponse.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
