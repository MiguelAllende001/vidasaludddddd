package com.vidasalud.report.service;

import com.vidasalud.report.kafka.AppointmentEventMessage;
import com.vidasalud.report.model.AppointmentEventRecord;
import com.vidasalud.report.repository.AppointmentEventRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private AppointmentEventRecordRepository repository;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(repository);
    }

    private AppointmentEventMessage mensaje(String eventId, Long appointmentId, String previousStatus, String newStatus) {
        AppointmentEventMessage message = new AppointmentEventMessage();
        message.setEventId(eventId);
        message.setAppointmentId(appointmentId);
        message.setServiceId(10L);
        message.setPreviousStatus(previousStatus);
        message.setNewStatus(newStatus);
        message.setOccurredAt(Instant.now());
        message.setTraceId("trace-1");
        message.setCorrelationId(String.valueOf(appointmentId));
        return message;
    }

    @Test
    void ingestaGuardaUnEventoNuevo() {
        when(repository.findByEventId("evt-1")).thenReturn(Optional.empty());

        service.ingest(mensaje("evt-1", 1L, null, "SOLICITADA"));

        verify(repository).save(any(AppointmentEventRecord.class));
    }

    @Test
    void ingestaEsIdempotente() {
        when(repository.findByEventId("evt-1")).thenReturn(Optional.of(new AppointmentEventRecord()));

        service.ingest(mensaje("evt-1", 1L, null, "SOLICITADA"));

        verify(repository, never()).save(any(AppointmentEventRecord.class));
    }

    @Test
    void calculaElTiempoDeEsperaPromedioEntreEnEsperaYEnAtencion() {
        Instant t0 = Instant.now().minus(2, ChronoUnit.HOURS);
        AppointmentEventRecord enEspera = AppointmentEventRecord.builder()
                .appointmentId(1L).serviceId(10L).newStatus("EN_ESPERA").occurredAt(t0).build();
        AppointmentEventRecord enAtencion = AppointmentEventRecord.builder()
                .appointmentId(1L).serviceId(10L).newStatus("EN_ATENCION").occurredAt(t0.plusSeconds(600)).build();

        when(repository.findByOccurredAtBetween(any(), any())).thenReturn(List.of(enEspera, enAtencion));

        var kpis = service.getKpis("last24h");

        assertThat(kpis.getAverageWaitMinutes()).isEqualTo(10.0);
        assertThat(kpis.getAppointmentsWithWaitTimeSample()).isEqualTo(1L);
    }

    @Test
    void soloCuentaEstadosActivosNoTerminales() {
        Instant now = Instant.now();
        AppointmentEventRecord activa = AppointmentEventRecord.builder()
                .appointmentId(1L).newStatus("EN_ESPERA").occurredAt(now).build();
        AppointmentEventRecord cerrada = AppointmentEventRecord.builder()
                .appointmentId(2L).newStatus("CERRADA").occurredAt(now).build();

        when(repository.findByOccurredAtBetween(any(), any())).thenReturn(List.of(activa, cerrada));

        var kpis = service.getKpis("last24h");

        assertThat(kpis.getActiveStates()).containsEntry("EN_ESPERA", 1L);
        assertThat(kpis.getActiveStates()).doesNotContainKey("CERRADA");
    }

    @Test
    void topServicesCuentaSoloCreacionesYOrdenaDescendente() {
        Instant now = Instant.now();
        AppointmentEventRecord creacionServicio10 = AppointmentEventRecord.builder()
                .appointmentId(1L).serviceId(10L).previousStatus(null).newStatus("SOLICITADA").occurredAt(now).build();
        AppointmentEventRecord otraCreacionServicio10 = AppointmentEventRecord.builder()
                .appointmentId(2L).serviceId(10L).previousStatus(null).newStatus("SOLICITADA").occurredAt(now).build();
        AppointmentEventRecord creacionServicio20 = AppointmentEventRecord.builder()
                .appointmentId(3L).serviceId(20L).previousStatus(null).newStatus("SOLICITADA").occurredAt(now).build();
        AppointmentEventRecord cambioDeEstado = AppointmentEventRecord.builder()
                .appointmentId(1L).serviceId(10L).previousStatus("SOLICITADA").newStatus("CONFIRMADA").occurredAt(now).build();

        when(repository.findByOccurredAtBetween(any(), any()))
                .thenReturn(List.of(creacionServicio10, otraCreacionServicio10, creacionServicio20, cambioDeEstado));

        var topServices = service.getTopServices("last7d", null);

        assertThat(topServices).hasSize(2);
        assertThat(topServices.get(0).getServiceId()).isEqualTo(10L);
        assertThat(topServices.get(0).getAppointmentCount()).isEqualTo(2L);
        assertThat(topServices.get(1).getServiceId()).isEqualTo(20L);
    }
}
