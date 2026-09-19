package com.vidasalud.notify.listener;

import com.vidasalud.notify.config.RabbitConfig;
import com.vidasalud.notify.dto.NotificationCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume q.cmd.admission: genera el ticket de admision / llamado a box
 * clinico cuando el paciente pasa a EN_ATENCION.
 */
@Slf4j
@Component
public class AdmissionTicketListener {

    @RabbitListener(queues = RabbitConfig.QUEUE_ADMISSION)
    public void onAdmissionCommand(NotificationCommand command) {
        if (command.getAppointmentId() == null || command.getBoxId() == null) {
            throw new IllegalArgumentException(
                    "Comando de admision invalido, falta appointmentId o boxId: " + command);
        }

        log.info(
                "[ADMISION] Ticket generado: paciente '{}' -> box #{} (atencion #{}, eventId={}, traceId={})",
                command.getPatientName(),
                command.getBoxId(),
                command.getAppointmentId(),
                command.getEventId(),
                command.getTraceId()
        );
        // TODO (futura evaluacion): imprimir/mostrar el ticket en la pantalla del box.
    }
}
