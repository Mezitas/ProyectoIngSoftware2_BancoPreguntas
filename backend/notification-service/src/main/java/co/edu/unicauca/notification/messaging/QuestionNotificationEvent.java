package co.edu.unicauca.notification.messaging;

import java.time.Instant;

/**
 * Evento de notificación sobre una pregunta. {@code type}: QUESTION_SUBMITTED (autor envió a revisión),
 * QUESTION_RESUBMITTED (autor reenvió corregida) o REVIEW_SUBMITTED (un revisor dejó su evaluación).
 */
public record QuestionNotificationEvent(String eventId, String type, Long questionId, Long recipientId,
                                        String recipientName, String recipientEmail, String actorName,
                                        String detail, Instant occurredAt) {
}
