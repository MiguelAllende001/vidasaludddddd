package com.vidasalud.report.repository;

import com.vidasalud.report.model.AppointmentEventRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentEventRecordRepository extends JpaRepository<AppointmentEventRecord, Long> {

    Optional<AppointmentEventRecord> findByEventId(String eventId);

    List<AppointmentEventRecord> findByOccurredAtBetween(Instant from, Instant to);
}
