package co.edu.unicauca.question.messaging;

import java.time.Instant;

public record OperationResultMessage(String operationId, Long questionId, String type, String status,
                                     String message, Instant completedAt) {
}
