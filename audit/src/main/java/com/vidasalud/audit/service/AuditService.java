package com.vidasalud.audit.service;

import com.vidasalud.audit.kafka.AuditTimelineMessage;
import com.vidasalud.audit.model.AuditEvent;
import com.vidasalud.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository repository;

    /**
     * Persiste un evento de audit.timeline de forma idempotente: si ya
     * llego un mensaje con el mismo eventId (reintento del productor,
     * redelivery de Kafka, etc.), no lo duplica.
     */
    public void record(AuditTimelineMessage message) {
        if (repository.findByEventId(message.getEventId()).isPresent()) {
            log.info("Evento de auditoria {} ya estaba registrado, se ignora (idempotencia)", message.getEventId());
            return;
        }

        AuditEvent event = AuditEvent.builder()
                .eventId(message.getEventId())
                .appointmentId(message.getAppointmentId())
                .eventType(message.getEventType())
                .previousStatus(message.getPreviousStatus())
                .newStatus(message.getNewStatus())
                .actorId(message.getActorId())
                .actorName(message.getActorName())
                .actorRole(message.getActorRole())
                .occurredAt(message.getOccurredAt() != null ? message.getOccurredAt() : Instant.now())
                .source(message.getSource())
                .traceId(message.getTraceId())
                .correlationId(message.getCorrelationId())
                .build();

        try {
            repository.save(event);
        } catch (DataIntegrityViolationException e) {
            // Carrera entre dos consumidores/reintentos: otro hilo ya lo guardo primero.
            log.warn("Evento de auditoria {} ya fue insertado por otro consumidor", message.getEventId());
        }
    }

    public List<AuditEvent> search(String actorId, String eventType, Instant from, Instant to) {
        Specification<AuditEvent> spec = (root, query, cb) -> cb.conjunction();

        if (actorId != null && !actorId.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actorId"), actorId));
        }
        if (eventType != null && !eventType.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("eventType"), eventType));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), to));
        }

        return repository.findAll(spec);
    }
}
