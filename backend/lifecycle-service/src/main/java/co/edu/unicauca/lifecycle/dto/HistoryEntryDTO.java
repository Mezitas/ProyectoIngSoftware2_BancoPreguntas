package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.QuestionStatus;
import java.time.Instant;

public record HistoryEntryDTO(QuestionStatus from, QuestionStatus to, Long changedBy, String comment,
                              String operationId, Instant changedAt) {
}
