package com.vidasalud.appointments.event;

import com.vidasalud.appointments.config.RabbitConfig;
import com.vidasalud.appointments.model.Appointment;
import com.vidasalud.appointments.model.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

/**
 * Punto unico donde appointments avisa al resto del ecosistema que algo paso
 * con una atencion:
 * <ul>
 *     <li>Kafka "appointments.events": para reporteria (KPIs).</li>
 *     <li>Kafka "audit.timeline": para auditoria (quien/que/cuando).</li>
 *     <li>RabbitMQ "cmd.direct": para que notify envie el email/ticket/comprobante.</li>
 * </ul>
 * Ninguna de estas publicaciones debe poder tumbar el flujo transaccional
 * principal (crear/cambiar estado de una atencion): si Kafka o RabbitMQ no
 * estan disponibles, se registra el error y se sigue adelante. Esto es
 * justamente lo que pide el caso: "datos por streaming sin bloquear el core".
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.appointments-events}")
    private String appointmentsEventsTopic;

    @Value("${app.kafka.topic.audit-timeline}")
    private String auditTimelineTopic;

    /**
     * Publica en Kafka (appointments.events + audit.timeline) el hecho de que
     * una atencion cambio de estado (o fue creada, si previousStatus es null).
     */
    public void publishAppointmentChanged(
            Appointment appointment,
            AppointmentStatus previousStatus,
            String actorId,
            String actorName,
            String actorRole,
            String traceId) {

        String eventId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        String newStatus = appointment.getStatus().name();
        String previous = previousStatus == null ? null : previousStatus.name();

        publishAppointmentEvent(eventId, appointment, previous, newStatus, traceId, now);
        publishAuditEvent(eventId, appointment, previous, newStatus, actorId, actorName, actorRole, traceId, now);
        publishNotificationCommandIfApplicable(appointment, newStatus, traceId, now);
    }

    private void publishAppointmentEvent(
            String eventId, Appointment appointment, String previousStatus, String newStatus,
            String traceId, Instant now) {
        try {
            AppointmentEventMessage message = AppointmentEventMessage.builder()
                    .eventId(eventId)
                    .appointmentId(appointment.getId())
                    .serviceId(appointment.getServiceId())
                    .boxId(appointment.getBoxId())
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .occurredAt(now)
                    .traceId(traceId)
                    .correlationId(String.valueOf(appointment.getId()))
                    .build();

            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(appointmentsEventsTopic, String.valueOf(appointment.getId()), json);
        } catch (Exception e) {
            log.error("No se pudo publicar en '{}' el evento de la atencion #{}: {}",
                    appointmentsEventsTopic, appointment.getId(), e.getMessage(), e);
        }
    }

    private void publishAuditEvent(
            String eventId, Appointment appointment, String previousStatus, String newStatus,
            String actorId, String actorName, String actorRole, String traceId, Instant now) {
        try {
            AuditTimelineMessage message = AuditTimelineMessage.builder()
                    .eventId(eventId)
                    .appointmentId(appointment.getId())
                    .eventType(previousStatus == null ? "ATTENTION_CREATED" : "STATUS_CHANGED")
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .actorId(actorId)
                    .actorName(actorName)
                    .actorRole(actorRole)
                    .occurredAt(now)
                    .source("ms-vidasalud-appointments")
                    .traceId(traceId)
                    .correlationId(String.valueOf(appointment.getId()))
                    .build();

            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(auditTimelineTopic, String.valueOf(appointment.getId()), json);
        } catch (Exception e) {
            log.error("No se pudo publicar en '{}' el evento de auditoria de la atencion #{}: {}",
                    auditTimelineTopic, appointment.getId(), e.getMessage(), e);
        }
    }

    /**
     * Recepcionista "confirma, llama a box y cierra" (Caso VidaSalud, punto 2):
     * cada una de esas 3 acciones dispara una notificacion asincrona distinta.
     */
    private void publishNotificationCommandIfApplicable(
            Appointment appointment, String newStatus, String traceId, Instant now) {

        String routingKey;
        String type;
        if (AppointmentStatus.CONFIRMADA.name().equals(newStatus)) {
            routingKey = RabbitConfig.ROUTING_EMAIL;
            type = "EMAIL_SEND";
        } else if (AppointmentStatus.EN_ATENCION.name().equals(newStatus)) {
            routingKey = RabbitConfig.ROUTING_ADMISSION;
            type = "ADMISSION_TICKET";
        } else if (AppointmentStatus.CERRADA.name().equals(newStatus)) {
            routingKey = RabbitConfig.ROUTING_RECORD;
            type = "RECORD_GEN";
        } else {
            return; // SOLICITADA, EN_ESPERA, CANCELADA no disparan notificacion en esta version.
        }

        try {
            NotificationCommand command = NotificationCommand.builder()
                    .eventId(UUID.randomUUID().toString())
                    .type(type)
                    .timestamp(now)
                    .traceId(traceId)
                    .correlationId(String.valueOf(appointment.getId()))
                    .appointmentId(appointment.getId())
                    .patientName(appointment.getPatientName())
                    .serviceId(appointment.getServiceId())
                    .boxId(appointment.getBoxId())
                    .scheduledAt(appointment.getScheduledAt())
                    .status(newStatus)
                    .build();

            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_DIRECT, routingKey, command);
        } catch (Exception e) {
            log.error("No se pudo publicar el comando '{}' de la atencion #{}: {}",
                    routingKey, appointment.getId(), e.getMessage(), e);
        }
    }
}
