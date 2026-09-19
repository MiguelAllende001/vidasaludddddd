package com.vidasalud.audit.service;

import com.vidasalud.audit.kafka.AuditTimelineMessage;
import com.vidasalud.audit.model.AuditEvent;
import com.vidasalud.audit.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository repository;

    private AuditService service;

    @BeforeEach
    void setUp() {
        service = new AuditService(repository);
    }

    private AuditTimelineMessage mensaje(String eventId) {
        AuditTimelineMessage message = new AuditTimelineMessage();
        message.setEventId(eventId);
        message.setAppointmentId(1L);
        message.setEventType("STATUS_CHANGED");
        message.setPreviousStatus("SOLICITADA");
        message.setNewStatus("CONFIRMADA");
        message.setActorId("user-1");
        message.setActorName("Ana");
        message.setActorRole("Operador");
        message.setOccurredAt(Instant.now());
        message.setSource("ms-vidasalud-appointments");
        message.setTraceId("trace-1");
        message.setCorrelationId("1");
        return message;
    }

    @Test
    void persisteUnEventoNuevo() {
        when(repository.findByEventId("evt-1")).thenReturn(Optional.empty());

        service.record(mensaje("evt-1"));

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo("evt-1");
        assertThat(captor.getValue().getNewStatus()).isEqualTo("CONFIRMADA");
    }

    @Test
    void esIdempotenteYNoDuplicaUnEventoYaRegistrado() {
        when(repository.findByEventId("evt-1")).thenReturn(Optional.of(new AuditEvent()));

        service.record(mensaje("evt-1"));

        verify(repository, never()).save(any(AuditEvent.class));
    }
}
