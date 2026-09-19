package com.vidasalud.notify.listener;

import com.vidasalud.notify.dto.NotificationCommand;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests puramente unitarios (no levantan Spring ni requieren un broker
 * RabbitMQ real): verifican la validacion de cada listener ante mensajes
 * bien y mal formados.
 */
class NotificationListenersTest {

    private NotificationCommand.NotificationCommandBuilder baseCommand() {
        return NotificationCommand.builder()
                .eventId("evt-1")
                .timestamp(Instant.now())
                .traceId("trace-1")
                .correlationId("1")
                .appointmentId(1L)
                .patientName("Juan Pérez")
                .serviceId(10L)
                .boxId(5L);
    }

    @Test
    void emailListenerAceptaUnComandoValido() {
        NotificationCommand command = baseCommand().type("EMAIL_SEND").status("CONFIRMADA").build();

        assertThatCode(() -> new EmailNotificationListener().onEmailCommand(command)).doesNotThrowAnyException();
    }

    @Test
    void emailListenerRechazaUnComandoSinPaciente() {
        NotificationCommand command = baseCommand().type("EMAIL_SEND").patientName(null).build();

        assertThatThrownBy(() -> new EmailNotificationListener().onEmailCommand(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void admissionListenerAceptaUnComandoValido() {
        NotificationCommand command = baseCommand().type("ADMISSION_TICKET").status("EN_ATENCION").build();

        assertThatCode(() -> new AdmissionTicketListener().onAdmissionCommand(command)).doesNotThrowAnyException();
    }

    @Test
    void admissionListenerRechazaUnComandoSinBox() {
        NotificationCommand command = baseCommand().type("ADMISSION_TICKET").boxId(null).build();

        assertThatThrownBy(() -> new AdmissionTicketListener().onAdmissionCommand(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordListenerAceptaUnComandoValido() {
        NotificationCommand command = baseCommand().type("RECORD_GEN").status("CERRADA").build();

        assertThatCode(() -> new RecordGenerationListener().onRecordCommand(command)).doesNotThrowAnyException();
    }

    @Test
    void recordListenerRechazaUnComandoSinAtencion() {
        NotificationCommand command = baseCommand().type("RECORD_GEN").appointmentId(null).build();

        assertThatThrownBy(() -> new RecordGenerationListener().onRecordCommand(command))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
