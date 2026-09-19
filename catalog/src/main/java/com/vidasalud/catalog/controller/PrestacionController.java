package com.vidasalud.catalog.controller;

import com.vidasalud.catalog.dto.PrestacionRequest;
import com.vidasalud.catalog.dto.PrestacionResponse;
import com.vidasalud.catalog.model.Prestacion;
import com.vidasalud.catalog.service.PrestacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/services")
@RequiredArgsConstructor
public class PrestacionController {

    private final PrestacionService service;

    // GET /api/catalog/services
    @GetMapping
    public ResponseEntity<List<PrestacionResponse>> findAll() {
        List<PrestacionResponse> results = service.findAll().stream()
                .map(PrestacionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrestacionResponse> getById(@PathVariable Long id) {
        Prestacion prestacion = service.getById(id);
        return ResponseEntity.ok(PrestacionResponse.fromEntity(prestacion));
    }

    // POST /api/catalog/services
    @PostMapping
    public ResponseEntity<PrestacionResponse> create(@Valid @RequestBody PrestacionRequest request) {
        Prestacion created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PrestacionResponse.fromEntity(created));
    }

    // PUT /api/catalog/services/{id} (precio/cupo)
    @PutMapping("/{id}")
    public ResponseEntity<PrestacionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PrestacionRequest request) {
        Prestacion updated = service.update(id, request);
        return ResponseEntity.ok(PrestacionResponse.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
