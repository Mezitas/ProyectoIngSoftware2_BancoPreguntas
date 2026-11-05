package co.edu.unicauca.notification.messaging;

import co.edu.unicauca.notification.config.RabbitMQConfig;
import co.edu.unicauca.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Consumidor asíncrono puro: nadie espera su respuesta. */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final NotificationService service;

    public NotificationListener(NotificationService service) {
        this.service = service;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REVIEWER_ASSIGNED)
    public void onReviewerAssigned(ReviewerAssignedEvent event) {
        log.info("[NotificationService] Evento ReviewerAssigned recibido: pregunta={} revisor={}",
                event.questionId(), event.reviewerId());
        service.handleReviewerAssigned(event);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_REVIEW_COMPLETED)
    public void onReviewCompleted(ReviewCompletedEvent event) {
        log.info("[NotificationService] Resultado de revisión recibido: pregunta={} destinatario={}",
                event.questionId(), event.recipientId());
        service.handleReviewCompleted(event);
    }
}
