package com.vidasalud.bff.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class SecurityConfig {

    // Roles definidos en el App Registration "vidasalud" de Azure AD
    // (ver Caso VidaSalud, punto 2. Actores y roles).
    private static final String ROL_ADMIN = "Admin";
    private static final String ROL_OPERADOR = "Operador";
    private static final String ROL_CLIENTE = "Cliente";
    private static final String ROL_AUDITOR = "Auditor";

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${azure.api-client-id}")
    private String apiClientId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()

                // --- Atenciones (ms-vidasalud-appointments) ---
                // Cambiar el estado de una atención (confirmar, llamar a box, cerrar, cancelar)
                // es una acción operativa: solo Admin/Recepcionista.
                .requestMatchers(HttpMethod.PUT, "/api/appointments/*/status")
                    .hasAnyAuthority(ROL_ADMIN, ROL_OPERADOR)
                // Listar/buscar atenciones (sala de espera, filtros por estado/fecha) es una
                // vista operativa: Admin/Recepcionista. El paciente consulta la suya por id.
                .requestMatchers(HttpMethod.GET, "/api/appointments")
                    .hasAnyAuthority(ROL_ADMIN, ROL_OPERADOR)
                // Solicitar una atención: la puede pedir el propio paciente, o
                // la recepcionista/admin en su nombre.
                .requestMatchers(HttpMethod.POST, "/api/appointments")
                    .hasAnyAuthority(ROL_ADMIN, ROL_OPERADOR, ROL_CLIENTE)
                // Consultar el detalle de una atención puntual.
                .requestMatchers(HttpMethod.GET, "/api/appointments/*")
                    .hasAnyAuthority(ROL_ADMIN, ROL_OPERADOR, ROL_CLIENTE)

                // --- Catálogo (ms-vidasalud-catalog) ---
                // Todos los roles autenticados necesitan poder LEER el catálogo de
                // prestaciones/boxes (para agendar o para mostrarlo en el dashboard).
                .requestMatchers(HttpMethod.GET, "/api/catalog/**")
                    .hasAnyAuthority(ROL_ADMIN, ROL_OPERADOR, ROL_CLIENTE)
                // Crear/editar/eliminar prestaciones, boxes y cupos es exclusivo de Admin
                // (ver Caso VidaSalud, punto 3. Alcance funcional mínimo: módulo Catálogo).
                .requestMatchers(HttpMethod.POST, "/api/catalog/**").hasAuthority(ROL_ADMIN)
                .requestMatchers(HttpMethod.PUT, "/api/catalog/**").hasAuthority(ROL_ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/catalog/**").hasAuthority(ROL_ADMIN)

                // --- Auditoria (ms-vidasalud-audit) ---
                // "Timeline de eventos... Auditor. Solo lectura" (Caso VidaSalud, punto 3).
                // Admin tambien puede verla (coordina toda la red).
                .requestMatchers(HttpMethod.GET, "/api/audit/**").hasAnyAuthority(ROL_ADMIN, ROL_AUDITOR)

                // --- Reporteria (ms-vidasalud-report) ---
                // "Panel de KPIs... Admin" (Caso VidaSalud, punto 3).
                .requestMatchers(HttpMethod.GET, "/api/report/**").hasAuthority(ROL_ADMIN)

                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                // Token ausente/inválido/expirado -> 401 (ver authenticationEntryPoint()).
                .authenticationEntryPoint(authenticationEntryPoint())
                // Token válido pero rol sin permiso sobre el endpoint -> 403
                // (ver accessDeniedHandler()). Como toda la API va detrás de
                // Bearer JWT, este único handler cubre ambos casos sin
                // necesidad de configurar exceptionHandling() por separado.
                .accessDeniedHandler(accessDeniedHandler())
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);

        // 1) Emisor correcto (Azure AD del tenant configurado) y vigencia del token
        //    (JwtValidators.createDefaultWithIssuer ya incluye el JwtTimestampValidator,
        //    que revisa exp/nbf con un pequeño margen de reloj).
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        // 2) Audiencia correcta: el token debe haber sido emitido para esta API
        //    (api://<API_CLIENT_ID>), no para otra aplicación cliente de Azure AD.
        OAuth2TokenValidator<Jwt> withAudience = new JwtClaimValidator<List<String>>(
                "aud", aud -> aud != null && aud.contains(apiClientId));

        // La firma del token (RS256 contra las llaves públicas del IDaaS) la valida
        // el propio NimbusJwtDecoder al construirse desde el issuer OIDC.
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        authoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    // Token ausente, mal formado, con firma inválida, expirado, emisor o
    // audiencia incorrectos -> 401 con un cuerpo JSON explicativo.
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> writeJsonError(
                response, HttpStatus.UNAUTHORIZED,
                "No autenticado: token ausente, inválido, expirado o mal emitido.");
    }

    // Token válido, pero el rol del usuario no tiene permiso sobre el endpoint -> 403.
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> writeJsonError(
                response, HttpStatus.FORBIDDEN,
                "Acceso denegado: el rol del usuario no está autorizado para este recurso.");
    }

    private void writeJsonError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        String json = "{\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + status.value() + ","
                + "\"error\":\"" + status.getReasonPhrase() + "\","
                + "\"message\":\"" + message + "\"}";
        response.getWriter().write(json);
    }
}
