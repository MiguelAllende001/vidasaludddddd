package com.vidasalud.notify.listener;

import com.vidasalud.notify.config.RabbitConfig;
import com.vidasalud.notify.dto.NotificationCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume q.cmd.record: genera el comprobante/PDF de resumen de la atencion
 * cuando esta se CIERRA.
 */
@Slf4j
@Component
public class RecordGenerationListener {

    @RabbitListener(queues = RabbitConfig.QUEUE_RECORD)
    public void onRecordCommand(NotificationCommand command) {
        if (command.getAppointmentId() == null) {
            throw new IllegalArgumentException(
                    "Comando de comprobante invalido, falta appointmentId: " + command);
        }

        log.info(
                "[COMPROBANTE] Generando resumen en PDF de la atencion #{} para '{}' (eventId={}, traceId={})",
                command.getAppointmentId(),
                command.getPatientName(),
                command.getEventId(),
                command.getTraceId()
        );
        // TODO (futura evaluacion): generar el PDF real y subirlo a almacenamiento (S3, etc.).
    }
}
