package com.vidasalud.appointments.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * "Fuente de verdad de eventos de la atencion" (Caso VidaSalud, punto 9,
 * topico appointments.events). Lo consume ms-vidasalud-report para calcular
 * KPIs (atenciones por hora, tiempo de espera, estados activos).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentEventMessage {

    private String eventId;
    private Long appointmentId;
    private Long serviceId;
    private Long boxId;
    private String previousStatus; // null cuando el evento es la creacion
    private String newStatus;
    private Instant occurredAt;
    private String traceId;
    private String correlationId;
}
