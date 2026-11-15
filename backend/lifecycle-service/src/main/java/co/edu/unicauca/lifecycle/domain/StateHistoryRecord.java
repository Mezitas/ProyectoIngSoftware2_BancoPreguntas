package co.edu.unicauca.lifecycle.domain;

import co.edu.unicauca.lifecycle.model.QuestionStatus;
import java.time.Instant;

/** Immutable domain record of one lifecycle transition. */
public record StateHistoryRecord(Long questionId, QuestionStatus fromStatus, QuestionStatus toStatus,
                                 Long changedBy, String comment, String operationId, Instant changedAt) {
}
