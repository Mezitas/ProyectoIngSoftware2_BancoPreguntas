package co.edu.unicauca.notification.service;

import co.edu.unicauca.notification.messaging.ReviewerAssignedEvent;
import co.edu.unicauca.notification.messaging.ReviewCompletedEvent;
import co.edu.unicauca.notification.model.NotificationEntity;
import co.edu.unicauca.notification.repository.NotificationRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public void handleReviewerAssigned(ReviewerAssignedEvent event) {
        if (repository.existsByEventId(event.eventId())) {
            log.warn("[NotificationService] Evento duplicado ignorado: {}", event.eventId());
            return;
        }
        String subject = "Nueva pregunta asignada para revisión";
        String message = "Hola " + event.reviewerName() + ", se le asignó la revisión de la pregunta #"
                + event.questionId() + ".";
        NotificationEntity n = repository.save(new NotificationEntity(event.eventId(), event.reviewerId(),
                event.reviewerEmail(), "REVIEWER_ASSIGNED", subject, message));
        try {
            emailService.send(event.reviewerEmail(), subject, message);
            n.markSent();
        } catch (RuntimeException e) {
            log.error("Falló el envío de la notificación {}", n.getId(), e);
            n.markFailed();
        }
        repository.save(n);
    }

    public void handleReviewCompleted(ReviewCompletedEvent event) {
        if (repository.existsByEventId(event.eventId())) {
            log.warn("[NotificationService] Evento duplicado ignorado: {}", event.eventId());
            return;
        }
        String subject = "Evaluación de la pregunta #" + event.questionId() + " finalizada";
        String message = "Hola " + event.recipientName() + ", la pregunta #" + event.questionId()
                + " fue " + ("APPROVED".equals(event.result()) ? "aprobada" : "rechazada") + "."
                + (event.comments() == null || event.comments().isBlank() ? "" : " Observaciones: " + event.comments());
        NotificationEntity notification = repository.save(new NotificationEntity(event.eventId(),
                event.recipientId(), event.recipientEmail(), "REVIEW_COMPLETED", subject, message));
        try {
            emailService.send(event.recipientEmail(), subject, message);
            notification.markSent();
        } catch (RuntimeException e) {
            log.error("Falló el envío de la notificación {}", notification.getId(), e);
            notification.markFailed();
        }
        repository.save(notification);
    }

    public List<NotificationEntity> findAll(Long recipientId) {
        return recipientId == null ? repository.findAll() : repository.findByRecipientIdOrderByIdAsc(recipientId);
    }
}
