package com.vidasalud.appointments.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Debe reflejar exactamente el mismo contrato JSON que espera
 * ms-vidasalud-notify (com.vidasalud.notify.dto.NotificationCommand): ambos
 * microservicios son independientes (no comparten un jar), asi que el
 * contrato es el JSON en si, serializado/deserializado por el mismo
 * JacksonJsonMessageConverter en ambos extremos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCommand {

    private String eventId;
    private String type; // EMAIL_SEND | ADMISSION_TICKET | RECORD_GEN
    private Instant timestamp;
    private String traceId;
    private String correlationId;

    private Long appointmentId;
    private String patientName;
    private Long serviceId;
    private Long boxId;
    private LocalDateTime scheduledAt;
    private String status;
}
