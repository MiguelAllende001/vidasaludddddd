package com.vidasalud.bff.controller;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Expone /api/report/* detras del BFF (Admin - ver SecurityConfig). Solo
 * lectura, tal como lo pide el Caso VidaSalud para el modulo Reporteria.
 */
@RestController
@RequestMapping("/api/report")
public class ReportBffController {

    private final RestClient reportClient;

    public ReportBffController(@Qualifier("reportClient") RestClient reportClient) {
        this.reportClient = reportClient;
    }

    @GetMapping("/kpis")
    public ResponseEntity<JsonNode> kpis(@RequestParam(required = false) String range) {
        return reportClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/report/kpis")
                        .queryParamIfPresent("range", Optional.ofNullable(range))
                        .build())
                .retrieve()
                .toEntity(JsonNode.class);
    }

    @GetMapping("/top-services")
    public ResponseEntity<JsonNode> topServices(
            @RequestParam(required = false) String range,
            @RequestParam(required = false) Integer limit) {
        return reportClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/report/top-services")
                        .queryParamIfPresent("range", Optional.ofNullable(range))
                        .queryParamIfPresent("limit", Optional.ofNullable(limit))
                        .build())
                .retrieve()
                .toEntity(JsonNode.class);
    }
}
