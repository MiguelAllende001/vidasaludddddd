package com.vidasalud.audit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Timeline de auditoria (Caso VidaSalud, punto 3: modulo Auditoria, actor
 * Auditor, "solo lectura"). Cada fila es un evento tal cual llego por Kafka
 * (topico audit.timeline), mas la marca de cuando lo persistimos.
 */
@Entity
@Table(name = "AUDIT_EVENTS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EVENT_ID", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "APPOINTMENT_ID", nullable = false)
    private Long appointmentId;

    @Column(name = "EVENT_TYPE", nullable = false, length = 40)
    private String eventType;

    @Column(name = "PREVIOUS_STATUS", length = 20)
    private String previousStatus;

    @Column(name = "NEW_STATUS", length = 20)
    private String newStatus;

    @Column(name = "ACTOR_ID", length = 100)
    private String actorId;

    @Column(name = "ACTOR_NAME", length = 150)
    private String actorName;

    @Column(name = "ACTOR_ROLE", length = 50)
    private String actorRole;

    @Column(name = "OCCURRED_AT", nullable = false)
    private Instant occurredAt;

    @Column(name = "SOURCE", length = 60)
    private String source;

    @Column(name = "TRACE_ID", length = 64)
    private String traceId;

    @Column(name = "CORRELATION_ID", length = 64)
    private String correlationId;

    @Column(name = "RECEIVED_AT", nullable = false, updatable = false)
    private Instant receivedAt;

    @PrePersist
    protected void onCreate() {
        if (this.receivedAt == null) {
            this.receivedAt = Instant.now();
        }
    }
}
