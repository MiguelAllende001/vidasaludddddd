package com.vidasalud.audit.kafka;

import com.vidasalud.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditTimelineConsumer {

    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    @KafkaListener(topics = "${app.kafka.topic.audit-timeline}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String rawJson) {
        try {
            AuditTimelineMessage message = objectMapper.readValue(rawJson, AuditTimelineMessage.class);
            auditService.record(message);
        } catch (Exception e) {
            // Un mensaje mal formado no debe tumbar el listener ni bloquear
            // el resto del topico: se registra el error y se sigue.
            log.error("No se pudo procesar un mensaje de audit.timeline: {} | payload={}",
                    e.getMessage(), rawJson, e);
        }
    }
}
