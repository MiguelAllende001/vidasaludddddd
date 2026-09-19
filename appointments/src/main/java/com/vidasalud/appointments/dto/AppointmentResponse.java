package com.vidasalud.appointments.dto;

import com.vidasalud.appointments.model.Appointment;
import com.vidasalud.appointments.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;
    private String patientName;
    private Long serviceId;
    private Long boxId;
    private LocalDateTime scheduledAt;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AppointmentResponse fromEntity(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientName(appointment.getPatientName())
                .serviceId(appointment.getServiceId())
                .boxId(appointment.getBoxId())
                .scheduledAt(appointment.getScheduledAt())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
