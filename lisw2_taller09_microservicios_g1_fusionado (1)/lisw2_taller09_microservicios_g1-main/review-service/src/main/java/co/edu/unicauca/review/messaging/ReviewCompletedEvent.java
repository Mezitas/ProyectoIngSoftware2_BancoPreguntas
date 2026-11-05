package co.edu.unicauca.review.messaging;

import java.time.Instant;

public record ReviewCompletedEvent(String eventId, Long questionId, Long recipientId, String recipientName,
                                   String recipientEmail, String result, String comments, Instant occurredAt) {
}
