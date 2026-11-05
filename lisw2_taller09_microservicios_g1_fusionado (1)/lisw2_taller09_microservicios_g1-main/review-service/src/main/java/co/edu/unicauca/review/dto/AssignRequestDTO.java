package co.edu.unicauca.review.dto;

import jakarta.validation.constraints.NotNull;

public record AssignRequestDTO(
        @NotNull(message = "questionId es obligatorio") Long questionId,
        @NotNull(message = "reviewerId es obligatorio") Long reviewerId,
        @NotNull(message = "assignedBy (administrador) es obligatorio") Long assignedBy) {
}
