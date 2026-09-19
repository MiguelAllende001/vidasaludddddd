package com.vidasalud.bff.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ServiceClientsConfig {

    @Bean
    @Qualifier("appointmentsClient")
    public RestClient appointmentsClient(
            @Value("${services.appointments.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    @Qualifier("catalogClient")
    public RestClient catalogClient(
            @Value("${services.catalog.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    @Qualifier("auditClient")
    public RestClient auditClient(
            @Value("${services.audit.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    @Qualifier("reportClient")
    public RestClient reportClient(
            @Value("${services.report.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
