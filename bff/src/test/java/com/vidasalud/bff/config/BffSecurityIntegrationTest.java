package com.vidasalud.bff.config;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el indicador de la pauta: "Configura correctamente el BFF para que...
 * pueda validar el token recibido con el IDaaS definido y solo permita consumir
 * el endpoint si el token es válido".
 *
 * No se prueba contra Azure AD real: se reemplaza el JwtDecoder por un mock
 * (así el test no depende de red ni de un tenant real) y se reemplazan los
 * microservicios de dominio por un servidor HTTP embebido de puro JDK que
 * responde siempre 2xx, para aislar la prueba a la capa de seguridad del BFF.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BffSecurityIntegrationTest {

    // Arranca en la carga de la clase (antes de cualquier callback de JUnit/Spring),
    // así su puerto ya existe cuando @DynamicPropertySource lo necesita.
    private static final HttpServer DOWNSTREAM = startFakeDownstreamServer();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void downstreamUrls(DynamicPropertyRegistry registry) {
        String baseUrl = "http://localhost:" + DOWNSTREAM.getAddress().getPort();
        registry.add("services.appointments.base-url", () -> baseUrl);
        registry.add("services.catalog.base-url", () -> baseUrl);
        registry.add("services.audit.base-url", () -> baseUrl);
        registry.add("services.report.base-url", () -> baseUrl);
    }

    @AfterAll
    static void stopDownstream() {
        DOWNSTREAM.stop(0);
    }

    private static HttpServer startFakeDownstreamServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                int status = switch (exchange.getRequestMethod()) {
                    case "POST" -> 201;
                    case "DELETE" -> 204;
                    default -> 200;
                };
                byte[] body = status == 204 ? new byte[0] : "{}".getBytes();
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
                if (body.length > 0) {
                    exchange.getResponseBody().write(body);
                }
                exchange.close();
            });
            server.setExecutor(null);
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo levantar el downstream falso para el test", e);
        }
    }

    private Jwt jwtConRoles(String... roles) {
        return Jwt.withTokenValue("token-de-prueba")
                .header("alg", "none")
                .claim("iss", "https://login.microsoftonline.com/test-tenant/v2.0")
                .claim("aud", "test-api-client-id")
                .claim("sub", "usuario-de-prueba")
                .claim("roles", List.of(roles))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void sinTokenSeRechazaConNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/catalog/services"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenValidoPeroRolInsuficienteSeRechazaConProhibido() throws Exception {
        when(jwtDecoder.decode("token-cliente")).thenReturn(jwtConRoles("Cliente"));

        // Cambiar el estado de una atención es operativo (Admin/Recepcionista),
        // el rol "Cliente" (paciente) no debería poder hacerlo.
        mockMvc.perform(put("/api/appointments/1/status")
                        .header("Authorization", "Bearer token-cliente")
                        .contentType("application/json")
                        .content("{\"status\":\"CONFIRMADA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeConsultarElCatalogo() throws Exception {
        when(jwtDecoder.decode("token-admin")).thenReturn(jwtConRoles("Admin"));

        mockMvc.perform(get("/api/catalog/services")
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk());
    }

    @Test
    void clientePuedeSolicitarUnaAtencionParaSiMismo() throws Exception {
        when(jwtDecoder.decode("token-cliente")).thenReturn(jwtConRoles("Cliente"));

        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer token-cliente")
                        .contentType("application/json")
                        .content("{\"patientName\":\"Juan Pérez\",\"serviceId\":1,\"scheduledAt\":\"2030-01-01T10:00:00\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void clienteNoPuedeModificarElCatalogo() throws Exception {
        when(jwtDecoder.decode("token-cliente")).thenReturn(jwtConRoles("Cliente"));

        mockMvc.perform(post("/api/catalog/services")
                        .header("Authorization", "Bearer token-cliente")
                        .contentType("application/json")
                        .content("{\"name\":\"Consulta\",\"price\":1000,\"availableSlots\":5}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditorPuedeConsultarLaAuditoria() throws Exception {
        when(jwtDecoder.decode("token-auditor")).thenReturn(jwtConRoles("Auditor"));

        mockMvc.perform(get("/api/audit/events")
                        .header("Authorization", "Bearer token-auditor"))
                .andExpect(status().isOk());
    }

    @Test
    void recepcionistaNoPuedeConsultarLaAuditoria() throws Exception {
        when(jwtDecoder.decode("token-operador")).thenReturn(jwtConRoles("Operador"));

        mockMvc.perform(get("/api/audit/events")
                        .header("Authorization", "Bearer token-operador"))
                .andExpect(status().isForbidden());
    }

    @Test
    void soloAdminPuedeConsultarLaReporteria() throws Exception {
        when(jwtDecoder.decode("token-admin")).thenReturn(jwtConRoles("Admin"));
        when(jwtDecoder.decode("token-auditor")).thenReturn(jwtConRoles("Auditor"));

        mockMvc.perform(get("/api/report/kpis")
                        .header("Authorization", "Bearer token-admin"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/report/kpis")
                        .header("Authorization", "Bearer token-auditor"))
                .andExpect(status().isForbidden());
    }
}
