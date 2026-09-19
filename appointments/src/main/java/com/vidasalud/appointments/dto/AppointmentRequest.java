package com.vidasalud.appointments.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequest {

    @NotBlank(message = "patientName es obligatorio")
    private String patientName;

    @NotNull(message = "serviceId es obligatorio")
    private Long serviceId;

    private Long boxId;

    @NotNull(message = "scheduledAt es obligatorio")
    @Future(message = "scheduledAt debe ser una fecha futura")
    private LocalDateTime scheduledAt;
}
