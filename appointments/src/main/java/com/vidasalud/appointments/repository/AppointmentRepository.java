package com.vidasalud.appointments.repository;

import com.vidasalud.appointments.model.Appointment;
import com.vidasalud.appointments.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    // JpaSpecificationExecutor nos permite armar el filtro dinámico
    // (status / from / to) en el service usando Specifications,
    // sin tener que escribir una query distinta por cada combinación.
}
