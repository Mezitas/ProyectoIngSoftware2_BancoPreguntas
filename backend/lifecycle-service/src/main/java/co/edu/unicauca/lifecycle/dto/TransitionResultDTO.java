package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.QuestionStatus;
import java.time.Instant;

public record TransitionResultDTO(Long questionId, QuestionStatus previousStatus, QuestionStatus status,
                                  String comment, Instant changedAt) {
}
