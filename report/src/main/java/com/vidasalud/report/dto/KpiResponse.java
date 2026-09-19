package com.vidasalud.report.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * "Panel de KPIs: atenciones por hora, tiempo de espera, estados activos"
 * (Caso VidaSalud, punto 3, modulo Reporteria).
 */
@Data
@Builder
public class KpiResponse {

    private String range;

    // Hora (ISO, truncada a la hora) -> cantidad de atenciones solicitadas en esa hora.
    private Map<String, Long> appointmentsPerHour;

    // Promedio, en minutos, entre que una atencion entra a EN_ESPERA y pasa a EN_ATENCION.
    // Null si no hay datos suficientes en el rango para calcularlo.
    private Double averageWaitMinutes;

    private Long appointmentsWithWaitTimeSample;

    // Estado -> cantidad de atenciones cuyo ultimo evento conocido (dentro del
    // rango) las deja en ese estado. Solo estados no terminales (activos).
    private Map<String, Long> activeStates;
}
