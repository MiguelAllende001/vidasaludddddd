package com.vidasalud.notify.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia RabbitMQ del Caso VidaSalud (punto 8): 3 colas principales + 3 DLQ,
 * repartidas entre un exchange direct y uno topic, con un exchange dead-letter
 * comun para las colas de reintento agotado.
 * <p>
 * El broker (infra/mq/compose.yml) ya carga esta misma topologia via
 * definitions.json al arrancar. Declararla tambien aqui es defensivo: si el
 * servicio se conecta a un broker "limpio" (por ejemplo en un test local),
 * RabbitAdmin la crea igual, de forma idempotente.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_DIRECT = "cmd.direct";
    public static final String EXCHANGE_TOPIC = "cmd.topic";
    public static final String EXCHANGE_DLX = "cmd.dead.dlx";

    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_ADMISSION = "q.cmd.admission";
    public static final String QUEUE_RECORD = "q.cmd.record";

    public static final String ROUTING_EMAIL = "email.send";
    public static final String ROUTING_ADMISSION = "admission.ticket";
    public static final String ROUTING_RECORD = "record.gen";

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setNullAsOptionalEmpty(true);
        return converter;
    }

    @Bean
    public Declarables cmdTopology() {
        DirectExchange direct = new DirectExchange(EXCHANGE_DIRECT, true, false);
        TopicExchange topic = new TopicExchange(EXCHANGE_TOPIC, true, false);
        DirectExchange dlx = new DirectExchange(EXCHANGE_DLX, true, false);

        Queue email = buildQueueWithDlq(QUEUE_EMAIL);
        Queue admission = buildQueueWithDlq(QUEUE_ADMISSION);
        Queue record = buildQueueWithDlq(QUEUE_RECORD);

        Queue emailDlq = new Queue(QUEUE_EMAIL + ".dlq", true);
        Queue admissionDlq = new Queue(QUEUE_ADMISSION + ".dlq", true);
        Queue recordDlq = new Queue(QUEUE_RECORD + ".dlq", true);

        return new Declarables(
                direct, topic, dlx,
                email, admission, record,
                emailDlq, admissionDlq, recordDlq,

                // Bindings "de comando" (un productor que sabe exactamente que quiere).
                BindingBuilder.bind(email).to(direct).with(ROUTING_EMAIL),
                BindingBuilder.bind(admission).to(direct).with(ROUTING_ADMISSION),
                BindingBuilder.bind(record).to(direct).with(ROUTING_RECORD),

                // Bindings "por patron" (permite variantes futuras, ej. email.reminder).
                BindingBuilder.bind(email).to(topic).with("email.*"),
                BindingBuilder.bind(admission).to(topic).with("admission.#"),
                BindingBuilder.bind(record).to(topic).with("record.*"),

                // Dead-letter: cada cola reenvia al exchange DLX con su propia routing key.
                BindingBuilder.bind(emailDlq).to(dlx).with(QUEUE_EMAIL + ".dlq"),
                BindingBuilder.bind(admissionDlq).to(dlx).with(QUEUE_ADMISSION + ".dlq"),
                BindingBuilder.bind(recordDlq).to(dlx).with(QUEUE_RECORD + ".dlq")
        );
    }

    private Queue buildQueueWithDlq(String queueName) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", queueName + ".dlq")
                .build();
    }
}
