package com.vidasalud.appointments.service;

import com.vidasalud.appointments.dto.AppointmentRequest;
import com.vidasalud.appointments.event.EventPublisherService;
import com.vidasalud.appointments.exception.AppointmentNotFoundException;
import com.vidasalud.appointments.exception.InvalidStatusTransitionException;
import com.vidasalud.appointments.model.Appointment;
import com.vidasalud.appointments.model.AppointmentStatus;
import com.vidasalud.appointments.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repository;
    private final EventPublisherService eventPublisher;

    // Máquina de estados: qué transiciones son válidas desde cada estado.
    // Esta es la regla clave del caso: no se puede llegar a EN_ATENCION
    // sin haber pasado antes por CONFIRMADA.
    private static final Map<AppointmentStatus, Set<AppointmentStatus>> TRANSITIONS = new EnumMap<>(AppointmentStatus.class);

    static {
        TRANSITIONS.put(AppointmentStatus.SOLICITADA, Set.of(AppointmentStatus.CONFIRMADA, AppointmentStatus.CANCELADA));
        TRANSITIONS.put(AppointmentStatus.CONFIRMADA, Set.of(AppointmentStatus.EN_ESPERA, AppointmentStatus.CANCELADA));
        TRANSITIONS.put(AppointmentStatus.EN_ESPERA, Set.of(AppointmentStatus.EN_ATENCION, AppointmentStatus.CANCELADA));
        TRANSITIONS.put(AppointmentStatus.EN_ATENCION, Set.of(AppointmentStatus.CERRADA));
        TRANSITIONS.put(AppointmentStatus.CERRADA, Set.of());
        TRANSITIONS.put(AppointmentStatus.CANCELADA, Set.of());
    }

    public Appointment create(AppointmentRequest request, String actorId, String actorName, String actorRole) {
        Appointment appointment = Appointment.builder()
                .patientName(request.getPatientName())
                .serviceId(request.getServiceId())
                .boxId(request.getBoxId())
                .scheduledAt(request.getScheduledAt())
                .status(AppointmentStatus.SOLICITADA)
                .build();
        Appointment created = repository.save(appointment);

        eventPublisher.publishAppointmentChanged(
                created, null, actorId, actorName, actorRole, UUID.randomUUID().toString());
        return created;
    }

    public Appointment getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    public Appointment updateStatus(
            Long id, AppointmentStatus newStatus, String actorId, String actorName, String actorRole) {
        Appointment appointment = getById(id);
        AppointmentStatus current = appointment.getStatus();

        Set<AppointmentStatus> allowed = TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "No se puede pasar de " + current + " a " + newStatus +
                            ". Transiciones válidas desde " + current + ": " + allowed);
        }

        appointment.setStatus(newStatus);
        Appointment updated = repository.save(appointment);

        eventPublisher.publishAppointmentChanged(
                updated, current, actorId, actorName, actorRole, UUID.randomUUID().toString());
        return updated;
    }

    public List<Appointment> search(AppointmentStatus status, LocalDateTime from, LocalDateTime to) {
        // En vez de Specification.where(null) (esta versión de Spring Data
        // ya no acepta null ahí), arrancamos con una condición neutra
        // (siempre verdadera) y le vamos agregando filtros con .and(...).
        Specification<Appointment> spec = (root, query, cb) -> cb.conjunction();

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("scheduledAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("scheduledAt"), to));
        }

        return repository.findAll(spec);
    }
}
