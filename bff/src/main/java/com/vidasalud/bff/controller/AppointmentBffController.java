package com.vidasalud.bff.controller;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentBffController {

    private final RestClient appointmentsClient;

    public AppointmentBffController(@Qualifier("appointmentsClient") RestClient appointmentsClient) {
        this.appointmentsClient = appointmentsClient;
    }

    @PostMapping
    public ResponseEntity<JsonNode> create(@RequestBody JsonNode body, @AuthenticationPrincipal Jwt jwt) {
        return appointmentsClient.post()
                .uri("/api/appointments")
                .headers(headers -> addActorHeaders(headers, jwt))
                .body(body)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> getById(@PathVariable Long id) {
        return appointmentsClient.get()
                .uri("/api/appointments/{id}", id)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<JsonNode> updateStatus(
            @PathVariable Long id, @RequestBody JsonNode body, @AuthenticationPrincipal Jwt jwt) {
        return appointmentsClient.put()
                .uri("/api/appointments/{id}/status", id)
                .headers(headers -> addActorHeaders(headers, jwt))
                .body(body)
                .retrieve()
                .toEntity(JsonNode.class);
    }

    @GetMapping
    public ResponseEntity<JsonNode> search(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return appointmentsClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/appointments")
                        .queryParamIfPresent("status", java.util.Optional.ofNullable(status))
                        .queryParamIfPresent("from", java.util.Optional.ofNullable(from))
                        .queryParamIfPresent("to", java.util.Optional.ofNullable(to))
                        .build())
                .retrieve()
                .toEntity(JsonNode.class);
    }

    /**
     * El BFF ya valido el JWT (issuer, audiencia, firma, vigencia); ahora le
     * pasa esa identidad a appointments para que pueda registrar "quien" hizo
     * la accion en audit.timeline (ver Caso VidaSalud, punto 9). Si por algun
     * motivo no hay JWT en el contexto (no deberia pasar detras de
     * SecurityConfig, pero por si acaso), simplemente no se agregan headers y
     * appointments usa sus valores por defecto ("desconocido").
     */
    private void addActorHeaders(HttpHeaders headers, Jwt jwt) {
        if (jwt == null) {
            return;
        }
        String actorId = firstNonBlank(jwt.getClaimAsString("oid"), jwt.getSubject());
        String actorName = firstNonBlank(
                jwt.getClaimAsString("name"), jwt.getClaimAsString("preferred_username"), actorId);
        List<String> roles = jwt.getClaimAsStringList("roles");
        String actorRoles = (roles == null || roles.isEmpty()) ? "N/A" : String.join(",", roles);

        headers.add("X-User-Id", actorId);
        headers.add("X-User-Name", actorName);
        headers.add("X-User-Roles", actorRoles);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "desconocido";
    }
}
