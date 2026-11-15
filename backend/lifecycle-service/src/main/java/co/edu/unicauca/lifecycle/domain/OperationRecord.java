package co.edu.unicauca.lifecycle.domain;

import co.edu.unicauca.lifecycle.model.OperationStatus;
import java.time.Instant;

/** Read model for the outcome of an asynchronous lifecycle command. */
public record OperationRecord(String operationId, Long questionId, String type, OperationStatus status,
                              String message, Instant createdAt, Instant completedAt) {
}
