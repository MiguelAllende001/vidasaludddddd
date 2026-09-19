package com.vidasalud.report.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Debe reflejar exactamente el contrato JSON publicado por
 * ms-vidasalud-appointments (com.vidasalud.appointments.event.AppointmentEventMessage)
 * en el topico appointments.events. No comparten jar: el contrato es el JSON.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEventMessage {

    private String eventId;
    private Long appointmentId;
    private Long serviceId;
    private Long boxId;
    private String previousStatus;
    private String newStatus;
    private Instant occurredAt;
    private String traceId;
    private String correlationId;
}
