package co.edu.unicauca.question.messaging;

import java.time.Instant;

/** Comando dirigido a Lifecycle Service: "intenta pasar esta pregunta de DRAFT a PENDING_REVIEW". */
public record SubmitQuestionForReviewCommand(String operationId, Long questionId, Long requestedBy,
                                             Instant requestedAt) {
}
