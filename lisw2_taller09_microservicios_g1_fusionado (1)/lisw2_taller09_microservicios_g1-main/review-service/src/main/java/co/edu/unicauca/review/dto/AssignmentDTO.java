package co.edu.unicauca.review.dto;

import co.edu.unicauca.review.model.AssignmentStatus;
import java.time.Instant;

public record AssignmentDTO(Long id, Long questionId, Long reviewerId, Long assignedBy, AssignmentStatus status,
                            String failureReason, Instant assignedAt, String notification) {
}
