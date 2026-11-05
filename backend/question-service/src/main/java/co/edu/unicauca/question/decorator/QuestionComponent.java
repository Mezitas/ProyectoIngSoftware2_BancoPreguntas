package co.edu.unicauca.question.decorator;

import co.edu.unicauca.question.domain.model.Question;

/** Patrón Decorator - componente: interfaz común de preguntas básicas y decoradas. */
public interface QuestionComponent {
    /** La pregunta original (sin modificar). */
    Question getQuestion();
    /** Texto presentable, incluyendo lo que añadan los decoradores. */
    String getDescription();
    /** Puntaje máximo (1.0 para una pregunta básica). */
    double getMaxScore();
    /** Explicación asociada (la justificación, ampliada por los decoradores). */
    String getExplanation();
}
