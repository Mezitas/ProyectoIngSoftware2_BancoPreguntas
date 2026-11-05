package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.OperationStatus;
import java.time.Instant;

public record OperationDTO(String operationId, Long questionId, String type, OperationStatus status,
                           String message, Instant createdAt, Instant completedAt) {
}
