package co.edu.unicauca.question.dto;

import java.time.Instant;

public record OperationDTO(String operationId, Long questionId, String type, String status, String message,
                           Instant createdAt, Instant completedAt) {
}
