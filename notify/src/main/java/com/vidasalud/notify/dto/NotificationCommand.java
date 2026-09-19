package com.vidasalud.notify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Envelope comun para los mensajes publicados por ms-vidasalud-appointments
 * en las colas q.cmd.email / q.cmd.admission / q.cmd.record (ver Caso
 * VidaSalud, punto 8: "envelope comun: type, eventId, timestamp, traceId,
 * correlationId"). Debe reflejar exactamente el mismo contrato JSON que
 * publica appointments (EventPublisherService), ya que no comparten un jar
 * comun: el contrato es el JSON, no el tipo Java.
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
    private String correlationId; // normalmente el id de la atencion

    private Long appointmentId;
    private String patientName;
    private Long serviceId;
    private Long boxId;
    private LocalDateTime scheduledAt;
    private String status;
}
