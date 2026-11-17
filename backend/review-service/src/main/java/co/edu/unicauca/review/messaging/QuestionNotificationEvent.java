package co.edu.unicauca.review.messaging;

import java.time.Instant;

/**
 * Evento de notificación sobre una pregunta. {@code type}: QUESTION_SUBMITTED, QUESTION_RESUBMITTED o
 * REVIEW_SUBMITTED. Notification Service arma el asunto y el mensaje según el tipo.
 */
public record QuestionNotificationEvent(String eventId, String type, Long questionId, Long recipientId,
                                        String recipientName, String recipientEmail, String actorName,
                                        String detail, Instant occurredAt) {
}
