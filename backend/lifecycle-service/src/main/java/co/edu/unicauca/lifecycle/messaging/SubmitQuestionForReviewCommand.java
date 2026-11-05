package co.edu.unicauca.lifecycle.messaging;

import java.time.Instant;

/** Comando recibido desde Question Service. */
public record SubmitQuestionForReviewCommand(String operationId, Long questionId, Long requestedBy,
                                             Instant requestedAt) {
}
