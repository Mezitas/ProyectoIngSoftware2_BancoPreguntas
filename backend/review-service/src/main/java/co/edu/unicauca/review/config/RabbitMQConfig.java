package co.edu.unicauca.review.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "saberpro.direct.exchange";

    public static final String QUEUE_REVIEWER_ASSIGNED = "notification.reviewer.assigned.queue";
    public static final String ROUTING_KEY_REVIEWER_ASSIGNED = "reviewer.assigned";
    public static final String QUEUE_REVIEW_COMPLETED = "notification.review.completed.queue";
    public static final String ROUTING_KEY_REVIEW_COMPLETED = "review.completed";
    public static final String QUEUE_QUESTION_EVENT = "notification.question.event.queue";
    public static final String ROUTING_KEY_QUESTION_EVENT = "question.event";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    /** Se declara aquí y en Notification Service (idéntica) para no perder eventos si éste aún no arrancó. */
    @Bean
    public Queue reviewerAssignedQueue() {
        return QueueBuilder.durable(QUEUE_REVIEWER_ASSIGNED).build();
    }

    @Bean
    public Binding bindingReviewerAssigned(Queue reviewerAssignedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(reviewerAssignedQueue).to(exchange).with(ROUTING_KEY_REVIEWER_ASSIGNED);
    }

    @Bean
    public Queue reviewCompletedQueue() {
        return QueueBuilder.durable(QUEUE_REVIEW_COMPLETED).build();
    }

    @Bean
    public Binding bindingReviewCompleted(Queue reviewCompletedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(reviewCompletedQueue).to(exchange).with(ROUTING_KEY_REVIEW_COMPLETED);
    }

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
