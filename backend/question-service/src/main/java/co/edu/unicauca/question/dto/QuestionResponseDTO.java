package co.edu.unicauca.question.dto;

import java.time.Instant;
import java.util.List;

/** {@code status} proviene de Lifecycle Service (o UNAVAILABLE si éste no responde). */
public record QuestionResponseDTO(
        Long id,
        Long authorId,
        String status,
        String context,
        String statement,
        List<OptionDTO> options,
        String correctOption,
        String justification,
        String competency,
        String topic,
        String subtopic,
        Instant createdAt,
        Instant updatedAt,
        String bibliography,
        String difficulty) {
}
