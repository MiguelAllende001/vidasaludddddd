package com.vidasalud.bff.controller;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/catalog/services")
public class PrestacionBffController {

    private final RestClient catalogClient;

    public PrestacionBffController(@Qualifier("catalogClient") RestClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    @GetMapping
    public ResponseEntity<JsonNode> findAll() {
        return catalogClient.get()
                .uri("/api/catalog/services")
                .retrieve()
                .toEntity(JsonNode.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> getById(@PathVariable Long id) {
        return catalogClient.get()
                .uri("/api/catalog/services/{id}", id)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    @PostMapping
    public ResponseEntity<JsonNode> create(@RequestBody JsonNode body) {
        return catalogClient.post()
                .uri("/api/catalog/services")
                .body(body)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JsonNode> update(@PathVariable Long id, @RequestBody JsonNode body) {
        return catalogClient.put()
                .uri("/api/catalog/services/{id}", id)
                .body(body)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catalogClient.delete()
                .uri("/api/catalog/services/{id}", id)
                .retrieve()
                .toBodilessEntity();
        return ResponseEntity.noContent().build();
    }
}
