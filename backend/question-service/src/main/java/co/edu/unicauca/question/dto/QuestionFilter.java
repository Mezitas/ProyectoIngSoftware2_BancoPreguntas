package co.edu.unicauca.question.dto;

/**
 * Criterios de búsqueda del listado de preguntas. Todos son opcionales; {@code text} busca en
 * enunciado, contexto, competencia, tema y subtema.
 */
public record QuestionFilter(Long authorId, String status, String text, String competency, String topic,
                             String subtopic, String difficulty) {

    public QuestionFilter withAuthorId(Long newAuthorId) {
        return new QuestionFilter(newAuthorId, status, text, competency, topic, subtopic, difficulty);
    }
}
