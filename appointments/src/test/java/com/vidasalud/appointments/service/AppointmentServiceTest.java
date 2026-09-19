package com.vidasalud.appointments.service;

import com.vidasalud.appointments.dto.AppointmentRequest;
import com.vidasalud.appointments.event.EventPublisherService;
import com.vidasalud.appointments.exception.AppointmentNotFoundException;
import com.vidasalud.appointments.exception.InvalidStatusTransitionException;
import com.vidasalud.appointments.model.Appointment;
import com.vidasalud.appointments.model.AppointmentStatus;
import com.vidasalud.appointments.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cubre la regla de negocio clave del caso VidaSalud: "no se puede pasar a
 * EN_ATENCION sin CONFIRMAR" (máquina de estados en AppointmentService).
 * <p>
 * EventPublisherService (Kafka/RabbitMQ) se mockea: estos tests validan
 * la máquina de estados, no la integración con la infraestructura de
 * mensajería (eso lo cubre BffSecurityIntegrationTest a otro nivel, y
 * requeriría un broker real para probarse de punta a punta).
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    @Mock
    private EventPublisherService eventPublisher;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        service = new AppointmentService(repository, eventPublisher);
    }

    private Appointment appointmentConEstado(AppointmentStatus status) {
        return Appointment.builder()
                .id(1L)
                .patientName("Juan Pérez")
                .serviceId(10L)
                .boxId(5L)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .status(status)
                .build();
    }

    @Test
    void crearAtencionQuedaEnEstadoSolicitadaYPublicaElEvento() {
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientName("Juan Pérez");
        request.setServiceId(10L);
        request.setBoxId(5L);
        request.setScheduledAt(LocalDateTime.now().plusDays(1));

        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment created = service.create(request, "user-1", "Juan Pérez", "Cliente");

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(repository).save(captor.capture());
        verify(eventPublisher).publishAppointmentChanged(
                any(Appointment.class), isNullStatus(), any(), any(), any(), any());

        assertThat(created.getStatus()).isEqualTo(AppointmentStatus.SOLICITADA);
        assertThat(captor.getValue().getPatientName()).isEqualTo("Juan Pérez");
    }

    @Test
    void noPermiteSaltarDeSolicitadaAEnAtencion() {
        Appointment existente = appointmentConEstado(AppointmentStatus.SOLICITADA);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.updateStatus(1L, AppointmentStatus.EN_ATENCION, "user-1", "Juan", "Operador"))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("SOLICITADA");
    }

    @Test
    void permiteConfirmarUnaAtencionSolicitadaYPublicaElEvento() {
        Appointment existente = appointmentConEstado(AppointmentStatus.SOLICITADA);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment actualizada = service.updateStatus(1L, AppointmentStatus.CONFIRMADA, "user-2", "Ana", "Operador");

        verify(eventPublisher).publishAppointmentChanged(
                any(Appointment.class), org.mockito.ArgumentMatchers.eq(AppointmentStatus.SOLICITADA),
                any(), any(), any(), any());
        assertThat(actualizada.getStatus()).isEqualTo(AppointmentStatus.CONFIRMADA);
    }

    @Test
    void permiteEntrarAAtencionSoloDespuesDeConfirmarYEsperar() {
        Appointment existente = appointmentConEstado(AppointmentStatus.EN_ESPERA);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment actualizada = service.updateStatus(1L, AppointmentStatus.EN_ATENCION, "user-2", "Ana", "Operador");

        assertThat(actualizada.getStatus()).isEqualTo(AppointmentStatus.EN_ATENCION);
    }

    @Test
    void noPermiteModificarUnaAtencionCerrada() {
        Appointment existente = appointmentConEstado(AppointmentStatus.CERRADA);
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.updateStatus(1L, AppointmentStatus.EN_ESPERA, "user-2", "Ana", "Operador"))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void getByIdLanzaExcepcionSiNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    private AppointmentStatus isNullStatus() {
        return org.mockito.ArgumentMatchers.isNull();
    }
}
