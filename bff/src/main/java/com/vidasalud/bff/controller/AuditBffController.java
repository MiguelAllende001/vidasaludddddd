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
 * Expone /api/audit/* detras del BFF (Admin, Auditor - ver SecurityConfig).
 * Solo lectura, tal como lo pide el Caso VidaSalud para el modulo Auditoria.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditBffController {

    private final RestClient auditClient;

    public AuditBffController(@Qualifier("auditClient") RestClient auditClient) {
        this.auditClient = auditClient;
    }

    @GetMapping("/events")
    public ResponseEntity<JsonNode> search(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return auditClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/audit/events")
                        .queryParamIfPresent("actorId", Optional.ofNullable(actorId))
                        .queryParamIfPresent("eventType", Optional.ofNullable(eventType))
                        .queryParamIfPresent("from", Optional.ofNullable(from))
                        .queryParamIfPresent("to", Optional.ofNullable(to))
                        .build())
                .retrieve()
                .toEntity(JsonNode.class);
    }
}
