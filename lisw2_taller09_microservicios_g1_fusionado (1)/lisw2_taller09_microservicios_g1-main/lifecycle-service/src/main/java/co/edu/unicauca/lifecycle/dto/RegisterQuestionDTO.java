package co.edu.unicauca.lifecycle.dto;

import jakarta.validation.constraints.NotNull;

public record RegisterQuestionDTO(
        @NotNull(message = "questionId es obligatorio") Long questionId,
        Long createdBy) {
}
