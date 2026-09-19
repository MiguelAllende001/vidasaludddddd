package com.vidasalud.audit.controller;

import com.vidasalud.audit.dto.AuditEventResponse;
import com.vidasalud.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * "Timeline de eventos de la atencion... Solo lectura" (Caso VidaSalud,
 * punto 3, modulo Auditoria, actor Auditor). No expone ningun POST/PUT/DELETE
 * a proposito.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService service;

    // GET /api/audit/events?actorId=...&eventType=...&from=...&to=...
    @GetMapping("/events")
    public ResponseEntity<List<AuditEventResponse>> search(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        List<AuditEventResponse> results = service.search(actorId, eventType, from, to).stream()
                .map(AuditEventResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(results);
    }
}
