package co.edu.unicauca.question.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Cuerpo de POST /questions y PUT /questions/{id}. La validez estructural la decide
 * Validation Service, incluyendo la bibliografía y el nivel de dificultad.
 */
public record QuestionRequestDTO(
        @NotNull(message = "authorId es obligatorio") Long authorId,
        String context,
        String statement,
        List<OptionDTO> options,
        String correctOption,
        String justification,
        String competency,
        String topic,
        String subtopic,
        String bibliography,
        String difficulty) {
}
