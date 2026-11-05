package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.QuestionStatus;
import java.time.Instant;

public record StateDTO(Long questionId, QuestionStatus status, Long createdBy, Long changedBy,
                       String comment, Instant changedAt) {
}
