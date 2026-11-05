package co.edu.unicauca.lifecycle.model;

/** Estados oficiales del ciclo de vida de una pregunta. */
public enum QuestionStatus {
    DRAFT,
    PENDING_REVIEW,
    IN_REVIEW,
    APPROVED,
    REJECTED,
    PUBLISHED
}
