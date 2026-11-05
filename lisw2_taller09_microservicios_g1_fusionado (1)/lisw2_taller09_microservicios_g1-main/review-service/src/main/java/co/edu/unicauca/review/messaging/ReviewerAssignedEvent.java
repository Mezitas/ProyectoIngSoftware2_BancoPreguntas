package co.edu.unicauca.review.messaging;

import java.time.Instant;

/** Evento de dominio: ya ocurrió una asignación confirmada. Lo consume Notification Service. */
public record ReviewerAssignedEvent(String eventId, Long questionId, Long reviewerId, String reviewerName,
                                    String reviewerEmail, Long assignedBy, Instant occurredAt) {
}
