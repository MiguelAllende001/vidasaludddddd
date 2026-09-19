package com.vidasalud.report.kafka;

import com.vidasalud.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEventsConsumer {

    private final ObjectMapper objectMapper;
    private final ReportService reportService;

    @KafkaListener(topics = "${app.kafka.topic.appointments-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String rawJson) {
        try {
            AppointmentEventMessage message = objectMapper.readValue(rawJson, AppointmentEventMessage.class);
            reportService.ingest(message);
        } catch (Exception e) {
            log.error("No se pudo procesar un mensaje de appointments.events: {} | payload={}",
                    e.getMessage(), rawJson, e);
        }
    }
}
