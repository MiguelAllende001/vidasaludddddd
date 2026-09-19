package com.vidasalud.notify.listener;

import com.vidasalud.notify.config.RabbitConfig;
import com.vidasalud.notify.dto.NotificationCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume q.cmd.email: notifica al paciente por email/push cuando su
 * atencion cambia de estado (tipicamente al CONFIRMAR).
 * <p>
 * Esta es una primera version que "simula" el envio (lo deja en el log de
 * forma estructurada). El punto de extension real seria inyectar aqui un
 * EmailSenderClient (SES, SendGrid, etc.) sin tocar el resto del flujo.
 */
@Slf4j
@Component
public class EmailNotificationListener {

    @RabbitListener(queues = RabbitConfig.QUEUE_EMAIL)
    public void onEmailCommand(NotificationCommand command) {
        if (command.getAppointmentId() == null || command.getPatientName() == null) {
            // Mensaje mal formado: no tiene sentido reintentarlo, se va directo a la DLQ.
            throw new IllegalArgumentException(
                    "Comando de email invalido, falta appointmentId o patientName: " + command);
        }

        log.info(
                "[EMAIL] Enviando notificacion a '{}' por la atencion #{} (estado={}, eventId={}, traceId={})",
                command.getPatientName(),
                command.getAppointmentId(),
                command.getStatus(),
                command.getEventId(),
                command.getTraceId()
        );
        // TODO (futura evaluacion): integrar un proveedor real de email/push.
    }
}
