package co.edu.unicauca.question.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "saberpro.direct.exchange";

    public static final String QUEUE_SUBMIT_REVIEW = "question.submit.review.queue";
    public static final String ROUTING_KEY_SUBMIT = "question.submit";

    public static final String QUEUE_OPERATION_RESULT = "question.operation.result.queue";
    public static final String ROUTING_KEY_OPERATION_RESULT = "question.operation-result";

    public static final String QUEUE_QUESTION_EVENT = "notification.question.event.queue";
    public static final String ROUTING_KEY_QUESTION_EVENT = "question.event";

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

    /** Se declara aquí y en Notification Service (idéntica) para no perder eventos si éste aún no arrancó. */
    @Bean
    public Queue questionEventQueue() {
        return QueueBuilder.durable(QUEUE_QUESTION_EVENT).build();
    }

    @Bean
    public Binding bindingQuestionEvent(Queue questionEventQueue, DirectExchange exchange) {
        return BindingBuilder.bind(questionEventQueue).to(exchange).with(ROUTING_KEY_QUESTION_EVENT);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
