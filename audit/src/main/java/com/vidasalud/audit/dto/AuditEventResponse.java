package com.vidasalud.audit.dto;

import com.vidasalud.audit.model.AuditEvent;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuditEventResponse {

    private Long id;
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

    public static AuditEventResponse fromEntity(AuditEvent event) {
        return AuditEventResponse.builder()
                .id(event.getId())
                .eventId(event.getEventId())
                .appointmentId(event.getAppointmentId())
                .eventType(event.getEventType())
                .previousStatus(event.getPreviousStatus())
                .newStatus(event.getNewStatus())
                .actorId(event.getActorId())
                .actorName(event.getActorName())
                .actorRole(event.getActorRole())
                .occurredAt(event.getOccurredAt())
                .source(event.getSource())
                .traceId(event.getTraceId())
                .correlationId(event.getCorrelationId())
                .build();
    }
}
