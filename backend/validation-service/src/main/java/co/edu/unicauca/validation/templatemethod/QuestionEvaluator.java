package co.edu.unicauca.validation.templatemethod;

import co.edu.unicauca.validation.decorator.QuestionComponent;
import co.edu.unicauca.validation.domain.model.Question;

/**
 * Patrón Template Method - clase abstracta.
 * {@link #evaluate(Question, String)} es el método plantilla (final).
 */
public abstract class QuestionEvaluator {
    public static final double BASE_MAX_SCORE = 1.0;

    public final EvaluationResult evaluate(Question question, String rawAnswer) {
        if (rawAnswer == null || rawAnswer.isBlank()) {
            return new EvaluationResult(false, 0.0, BASE_MAX_SCORE,
                    "No se recibió respuesta. Se esperaba: " + expectedFormat());
        }
        String answer = rawAnswer.trim();
        if (!isValidFormat(answer)) {
            return new EvaluationResult(false, 0.0, BASE_MAX_SCORE,
                    "Formato de respuesta inválido ('" + answer + "'). Se esperaba: " + expectedFormat());
        }
        double score = calculateScore(question, answer);
        return new EvaluationResult(true, score, BASE_MAX_SCORE, buildFeedback(question, answer, score));
    }

    public final EvaluationResult evaluate(QuestionComponent component, String rawAnswer) {
        EvaluationResult base = evaluate(component.getQuestion(), rawAnswer);
        double factor = component.getMaxScore() / BASE_MAX_SCORE;
        return base.scaledBy(factor);
    }

    protected abstract boolean isValidFormat(String answer);
    protected abstract double calculateScore(Question question, String answer);

    protected String expectedFormat() { return "una respuesta válida"; }

    protected String buildFeedback(Question question, String answer, double score) {
        String justification = question.getJustification();
        return (justification == null || justification.isBlank()) ? "Sin justificación registrada." : justification;
    }
}
