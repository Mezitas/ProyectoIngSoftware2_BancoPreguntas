package co.edu.unicauca.notification.messaging;

import java.time.Instant;

public record ReviewerAssignedEvent(String eventId, Long questionId, Long reviewerId, String reviewerName,
                                    String reviewerEmail, Long assignedBy, Instant occurredAt) {
}
