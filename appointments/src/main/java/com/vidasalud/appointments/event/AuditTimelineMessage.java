package com.vidasalud.appointments.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * "Historial quien / que / cuando / desde donde" (Caso VidaSalud, punto 9,
 * topico audit.timeline). Lo consume ms-vidasalud-audit y lo persiste
 * tal cual para la pantalla de Auditoria (solo lectura).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditTimelineMessage {

    private String eventId;
    private Long appointmentId;
    private String eventType; // ATTENTION_CREATED | STATUS_CHANGED
    private String previousStatus;
    private String newStatus;
    private String actorId;
    private String actorName;
    private String actorRole;
    private Instant occurredAt;
    private String source; // microservicio que origino el evento
    private String traceId;
    private String correlationId;
}
