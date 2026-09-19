package com.vidasalud.audit.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Debe reflejar exactamente el contrato JSON publicado por
 * ms-vidasalud-appointments (com.vidasalud.appointments.event.AuditTimelineMessage)
 * en el topico audit.timeline. No comparten jar: el contrato es el JSON.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditTimelineMessage {

    private String eventId;
    private Long appointmentId;
    private String eventType;
    private String previousStatus;
    private String newStatus;
    private String actorId;
    private String actorName;
    private String actorRole;
    private Instant occurredAt;
    private String source;
    private String traceId;
    private String correlationId;
}
