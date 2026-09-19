package com.vidasalud.report.controller;

import com.vidasalud.report.dto.KpiResponse;
import com.vidasalud.report.dto.TopServiceResponse;
import com.vidasalud.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * "Panel de KPIs... Admin. Datos por streaming (Kafka) sin bloquear el core"
 * (Caso VidaSalud, punto 3, modulo Reporteria). Solo lectura.
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    // GET /api/report/kpis?range=last24h
    @GetMapping("/kpis")
    public ResponseEntity<KpiResponse> kpis(@RequestParam(required = false, defaultValue = "last24h") String range) {
        return ResponseEntity.ok(service.getKpis(range));
    }

    // GET /api/report/top-services?range=last7d&limit=5
    @GetMapping("/top-services")
    public ResponseEntity<List<TopServiceResponse>> topServices(
            @RequestParam(required = false, defaultValue = "last7d") String range,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(service.getTopServices(range, limit));
    }
}
