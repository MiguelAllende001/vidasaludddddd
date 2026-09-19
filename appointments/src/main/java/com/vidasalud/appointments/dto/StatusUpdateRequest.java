package com.vidasalud.appointments.dto;

import com.vidasalud.appointments.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {

    @NotNull(message = "status es obligatorio")
    private AppointmentStatus status;
}
