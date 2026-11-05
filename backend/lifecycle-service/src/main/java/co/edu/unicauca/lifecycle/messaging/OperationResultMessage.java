package co.edu.unicauca.lifecycle.messaging;

import java.time.Instant;

/** Resultado del procesamiento, publicado hacia Question Service. status = COMPLETED | FAILED. */
public record OperationResultMessage(String operationId, Long questionId, String type, String status,
                                     String message, Instant completedAt) {
}
