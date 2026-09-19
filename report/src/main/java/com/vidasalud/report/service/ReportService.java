package com.vidasalud.report.service;

import com.vidasalud.report.dto.KpiResponse;
import com.vidasalud.report.dto.TopServiceResponse;
import com.vidasalud.report.kafka.AppointmentEventMessage;
import com.vidasalud.report.model.AppointmentEventRecord;
import com.vidasalud.report.repository.AppointmentEventRecordRepository;
import com.vidasalud.report.util.RangeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    // Solo estados no terminales cuentan como "activos" en el panel de KPIs.
    private static final Set<String> ACTIVE_STATES = Set.of("SOLICITADA", "CONFIRMADA", "EN_ESPERA", "EN_ATENCION");
    private static final int DEFAULT_TOP_SERVICES_LIMIT = 5;

    private final AppointmentEventRecordRepository repository;

    /**
     * Guarda el evento crudo de forma idempotente (mismo criterio que
     * ms-vidasalud-audit): un eventId repetido (reintento, redelivery) no
     * genera una fila duplicada.
     */
    public void ingest(AppointmentEventMessage message) {
        if (repository.findByEventId(message.getEventId()).isPresent()) {
            log.info("Evento {} ya estaba registrado en reporteria, se ignora (idempotencia)", message.getEventId());
            return;
        }

        AppointmentEventRecord record = AppointmentEventRecord.builder()
                .eventId(message.getEventId())
                .appointmentId(message.getAppointmentId())
                .serviceId(message.getServiceId())
                .boxId(message.getBoxId())
                .previousStatus(message.getPreviousStatus())
                .newStatus(message.getNewStatus())
                .occurredAt(message.getOccurredAt() != null ? message.getOccurredAt() : Instant.now())
                .build();

        try {
            repository.save(record);
        } catch (DataIntegrityViolationException e) {
            log.warn("Evento {} ya fue insertado por otro consumidor", message.getEventId());
        }
    }

    public KpiResponse getKpis(String range) {
        Duration window = RangeParser.parse(range);
        Instant to = Instant.now();
        Instant from = to.minus(window);

        List<AppointmentEventRecord> events = repository.findByOccurredAtBetween(from, to);

        return KpiResponse.builder()
                .range(range)
                .appointmentsPerHour(appointmentsPerHour(events))
                .averageWaitMinutes(averageWaitMinutes(events))
                .appointmentsWithWaitTimeSample((long) waitDurations(events).size())
                .activeStates(activeStates(events))
                .build();
    }

    public List<TopServiceResponse> getTopServices(String range, Integer limit) {
        Duration window = RangeParser.parse(range);
        Instant to = Instant.now();
        Instant from = to.minus(window);
        int effectiveLimit = (limit != null && limit > 0) ? limit : DEFAULT_TOP_SERVICES_LIMIT;

        List<AppointmentEventRecord> events = repository.findByOccurredAtBetween(from, to);

        Map<Long, Long> countByService = events.stream()
                .filter(e -> e.getPreviousStatus() == null) // solo la creacion cuenta como "demanda" del servicio
                .filter(e -> e.getServiceId() != null)
                .collect(Collectors.groupingBy(AppointmentEventRecord::getServiceId, Collectors.counting()));

        return countByService.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(effectiveLimit)
                .map(entry -> TopServiceResponse.builder()
                        .serviceId(entry.getKey())
                        .appointmentCount(entry.getValue())
                        .build())
                .toList();
    }

    private Map<String, Long> appointmentsPerHour(List<AppointmentEventRecord> events) {
        return events.stream()
                .filter(e -> e.getPreviousStatus() == null) // evento de creacion de la atencion
                .collect(Collectors.groupingBy(
                        e -> e.getOccurredAt().truncatedTo(ChronoUnit.HOURS).toString(),
                        TreeMap::new,
                        Collectors.counting()));
    }

    private List<Duration> waitDurations(List<AppointmentEventRecord> events) {
        Map<Long, Instant> enEsperaByAppointment = new HashMap<>();
        Map<Long, Instant> enAtencionByAppointment = new HashMap<>();

        for (AppointmentEventRecord event : events) {
            if ("EN_ESPERA".equals(event.getNewStatus())) {
                enEsperaByAppointment.put(event.getAppointmentId(), event.getOccurredAt());
            } else if ("EN_ATENCION".equals(event.getNewStatus())) {
                enAtencionByAppointment.put(event.getAppointmentId(), event.getOccurredAt());
            }
        }

        List<Duration> durations = new ArrayList<>();
        for (Map.Entry<Long, Instant> entry : enEsperaByAppointment.entrySet()) {
            Instant enAtencion = enAtencionByAppointment.get(entry.getKey());
            if (enAtencion != null && enAtencion.isAfter(entry.getValue())) {
                durations.add(Duration.between(entry.getValue(), enAtencion));
            }
        }
        return durations;
    }

    private Double averageWaitMinutes(List<AppointmentEventRecord> events) {
        List<Duration> durations = waitDurations(events);
        if (durations.isEmpty()) {
            return null;
        }
        double averageSeconds = durations.stream()
                .mapToLong(Duration::getSeconds)
                .average()
                .orElse(0);
        return averageSeconds / 60.0;
    }

    private Map<String, Long> activeStates(List<AppointmentEventRecord> events) {
        // Ultimo evento conocido (dentro del rango) por atencion.
        Map<Long, AppointmentEventRecord> lastEventByAppointment = new HashMap<>();
        for (AppointmentEventRecord event : events) {
            lastEventByAppointment.merge(event.getAppointmentId(), event,
                    (a, b) -> a.getOccurredAt().isAfter(b.getOccurredAt()) ? a : b);
        }

        return lastEventByAppointment.values().stream()
                .map(AppointmentEventRecord::getNewStatus)
                .filter(ACTIVE_STATES::contains)
                .collect(Collectors.groupingBy(status -> status, TreeMap::new, Collectors.counting()));
    }
}
