package co.edu.unicauca.lifecycle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología AMQP. Las colas que comparte con otros servicios se declaran de forma
 * idéntica en cada uno (la declaración es idempotente), así ningún mensaje se pierde
 * aunque el otro servicio todavía no haya arrancado.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "saberpro.direct.exchange";

    public static final String QUEUE_SUBMIT_REVIEW = "question.submit.review.queue";
    public static final String ROUTING_KEY_SUBMIT = "question.submit";

    public static final String QUEUE_OPERATION_RESULT = "question.operation.result.queue";
    public static final String ROUTING_KEY_OPERATION_RESULT = "question.operation-result";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue submitQueue() {
        return QueueBuilder.durable(QUEUE_SUBMIT_REVIEW).build();
    }

    @Bean
    public Queue operationResultQueue() {
        return QueueBuilder.durable(QUEUE_OPERATION_RESULT).build();
    }

    @Bean
    public Binding bindingSubmit(Queue submitQueue, DirectExchange exchange) {
        return BindingBuilder.bind(submitQueue).to(exchange).with(ROUTING_KEY_SUBMIT);
    }

    @Bean
    public Binding bindingOperationResult(Queue operationResultQueue, DirectExchange exchange) {
        return BindingBuilder.bind(operationResultQueue).to(exchange).with(ROUTING_KEY_OPERATION_RESULT);
    }

    /** Serialización JSON; usa el ObjectMapper de Spring Boot (soporta Instant / fechas ISO). */
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
