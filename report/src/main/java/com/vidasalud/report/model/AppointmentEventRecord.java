package com.vidasalud.report.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Copia local (read model) de cada evento de "appointments.events" (Caso
 * VidaSalud, punto 9: "Fuente de verdad de eventos de la atencion. Alimenta
 * reporteria..."). Las agregaciones de KPIs se calculan sobre esta tabla,
 * nunca sobre la base transaccional de appointments: eso es justamente lo
 * que permite que la reporteria no bloquee el core.
 */
@Entity
@Table(name = "APPOINTMENT_EVENT_RECORDS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EVENT_ID", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "APPOINTMENT_ID", nullable = false)
    private Long appointmentId;

    @Column(name = "SERVICE_ID")
    private Long serviceId;

    @Column(name = "BOX_ID")
    private Long boxId;

    @Column(name = "PREVIOUS_STATUS", length = 20)
    private String previousStatus;

    @Column(name = "NEW_STATUS", nullable = false, length = 20)
    private String newStatus;

    @Column(name = "OCCURRED_AT", nullable = false)
    private Instant occurredAt;

    @Column(name = "RECEIVED_AT", nullable = false, updatable = false)
    private Instant receivedAt;

    @PrePersist
    protected void onCreate() {
        if (this.receivedAt == null) {
            this.receivedAt = Instant.now();
        }
    }
}
