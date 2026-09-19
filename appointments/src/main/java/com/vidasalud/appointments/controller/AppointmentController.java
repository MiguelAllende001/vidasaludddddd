package com.vidasalud.appointments.controller;

import com.vidasalud.appointments.dto.AppointmentRequest;
import com.vidasalud.appointments.dto.AppointmentResponse;
import com.vidasalud.appointments.dto.StatusUpdateRequest;
import com.vidasalud.appointments.model.Appointment;
import com.vidasalud.appointments.model.AppointmentStatus;
import com.vidasalud.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    // El BFF ya valido el JWT y conoce quien esta llamando; nos reenvia esa
    // identidad en headers para poder auditar "quien" hizo cada accion (ver
    // Caso VidaSalud, punto 9: audit.timeline = "quien/que/cuando/desde donde").
    // Si el endpoint se llama directo (dev/tests, sin pasar por el BFF), se
    // deja constancia de que el actor es "desconocido" en vez de fallar.
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_ROLES = "X-User-Roles";

    // POST /api/appointments
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            @Valid @RequestBody AppointmentRequest request,
            @RequestHeader(value = HEADER_USER_ID, required = false, defaultValue = "desconocido") String actorId,
            @RequestHeader(value = HEADER_USER_NAME, required = false, defaultValue = "Desconocido") String actorName,
            @RequestHeader(value = HEADER_USER_ROLES, required = false, defaultValue = "N/A") String actorRole) {
        Appointment created = service.create(request, actorId, actorName, actorRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.fromEntity(created));
    }

    // GET /api/appointments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable Long id) {
        Appointment appointment = service.getById(id);
        return ResponseEntity.ok(AppointmentResponse.fromEntity(appointment));
    }

    // PUT /api/appointments/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @RequestHeader(value = HEADER_USER_ID, required = false, defaultValue = "desconocido") String actorId,
            @RequestHeader(value = HEADER_USER_NAME, required = false, defaultValue = "Desconocido") String actorName,
            @RequestHeader(value = HEADER_USER_ROLES, required = false, defaultValue = "N/A") String actorRole) {
        Appointment updated = service.updateStatus(id, request.getStatus(), actorId, actorName, actorRole);
        return ResponseEntity.ok(AppointmentResponse.fromEntity(updated));
    }

    // GET /api/appointments?status=...&from=...&to=...
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> search(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<AppointmentResponse> results = service.search(status, from, to).stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(results);
    }
}
