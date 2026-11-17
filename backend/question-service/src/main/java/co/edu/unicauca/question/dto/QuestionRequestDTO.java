package co.edu.unicauca.question.dto;

import java.util.List;

/**
 * Cuerpo de POST /questions y PUT /questions/{id}. La validez estructural la decide
 * Validation Service, incluyendo la bibliografía y el nivel de dificultad.
 */
public record QuestionRequestDTO(
        Long authorId,
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

    /** Copia de la petición con el autor tomado del JWT. */
    public QuestionRequestDTO withAuthorId(Long newAuthorId) {
        return new QuestionRequestDTO(newAuthorId, context, statement, options, correctOption, justification,
                competency, topic, subtopic, bibliography, difficulty);
    }
}
