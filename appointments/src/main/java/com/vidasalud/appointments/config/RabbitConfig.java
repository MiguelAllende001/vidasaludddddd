package com.vidasalud.appointments.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * appointments es el PRODUCTOR de los comandos de notificacion (ver Caso
 * VidaSalud, punto 8). Solo necesita que existan los exchanges (las colas y
 * sus bindings son responsabilidad del consumidor, ms-vidasalud-notify, y del
 * broker via infra/mq/definitions.json). Declararlos aqui tambien es
 * defensivo/idempotente: si ya existen con la misma configuracion, no pasa
 * nada.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";

    public static final String ROUTING_EMAIL = "email.send";
    public static final String ROUTING_ADMISSION = "admission.ticket";
    public static final String ROUTING_RECORD = "record.gen";

    @Bean
    public DirectExchange cmdDirectExchange() {
        return new DirectExchange(EXCHANGE_DIRECT, true, false);
    }

    @Bean
    public TopicExchange cmdTopicExchange() {
        return new TopicExchange(EXCHANGE_TOPIC, true, false);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setNullAsOptionalEmpty(true);
        return converter;
    }
}
