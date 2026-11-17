package co.edu.unicauca.notification.service;

import co.edu.unicauca.notification.messaging.QuestionNotificationEvent;
import co.edu.unicauca.notification.messaging.ReviewCompletedEvent;
import co.edu.unicauca.notification.messaging.ReviewerAssignedEvent;
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
        String subject = "Nueva pregunta asignada para revisión";
        String message = "Hola " + event.reviewerName() + ", se le asignó la revisión de la pregunta #"
                + event.questionId() + ".";
        dispatch(event.eventId(), event.reviewerId(), event.reviewerEmail(), "REVIEWER_ASSIGNED", subject, message);
    }

    public void handleReviewCompleted(ReviewCompletedEvent event) {
        String subject = "Evaluación de la pregunta #" + event.questionId() + " finalizada";
        String message = "Hola " + event.recipientName() + ", la pregunta #" + event.questionId()
                + " fue " + ("APPROVED".equals(event.result()) ? "aprobada" : "rechazada") + "."
                + (event.comments() == null || event.comments().isBlank() ? "" : " Observaciones: " + event.comments());
        dispatch(event.eventId(), event.recipientId(), event.recipientEmail(), "REVIEW_COMPLETED", subject, message);
    }

    /** Envío a revisión, reenvío tras corregir y evaluación individual de un revisor. */
    public void handleQuestionEvent(QuestionNotificationEvent event) {
        String subject;
        String message;
        switch (event.type()) {
            case "QUESTION_SUBMITTED" -> {
                subject = "Pregunta #" + event.questionId() + " pendiente de revisión";
                message = "Hola " + event.recipientName() + ", " + event.actorName() + " envió la pregunta #"
                        + event.questionId() + " a revisión. Asígnele entre 1 y 3 revisores.";
            }
            case "QUESTION_RESUBMITTED" -> {
                subject = "Pregunta #" + event.questionId() + " reenviada tras correcciones";
                message = "Hola " + event.recipientName() + ", " + event.actorName() + " reenvió corregida la pregunta #"
                        + event.questionId() + ". Asígnele nuevamente entre 1 y 3 revisores.";
            }
            case "REVIEW_SUBMITTED" -> {
                subject = "Nueva evaluación de la pregunta #" + event.questionId();
                message = "Hola " + event.recipientName() + ", " + event.actorName() + " " + event.detail();
            }
            default -> {
                log.warn("[NotificationService] Tipo de evento desconocido ignorado: {}", event.type());
                return;
            }
        }
        dispatch(event.eventId(), event.recipientId(), event.recipientEmail(), event.type(), subject, message);
    }

    /** Persiste la notificación (idempotente por eventId) y la envía por correo; registra SENT o FAILED. */
    private void dispatch(String eventId, Long recipientId, String recipientEmail, String type, String subject,
                          String message) {
        if (repository.existsByEventId(eventId)) {
            log.warn("[NotificationService] Evento duplicado ignorado: {}", eventId);
            return;
        }
        NotificationEntity notification = repository.save(
                new NotificationEntity(eventId, recipientId, recipientEmail, type, subject, message));
        try {
            emailService.send(recipientEmail, subject, message);
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

    /**
     * Un administrador consulta cualquier destinatario; los demás usuarios solo ven las notificaciones enviadas
     * a su propio correo (claim email del JWT), sin importar el recipientId que pidan.
     */
    public List<NotificationEntity> findVisible(Long recipientId, String requesterEmail, boolean admin) {
        if (admin) {
            return findAll(recipientId);
        }
        if (requesterEmail == null || requesterEmail.isBlank()) {
            return List.of();
        }
        List<NotificationEntity> own = repository.findByRecipientEmailIgnoreCaseOrderByIdAsc(requesterEmail);
        return recipientId == null ? own
                : own.stream().filter(n -> recipientId.equals(n.getRecipientId())).toList();
    }
}
