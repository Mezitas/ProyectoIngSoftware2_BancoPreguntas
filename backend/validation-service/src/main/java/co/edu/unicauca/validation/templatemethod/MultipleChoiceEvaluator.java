package co.edu.unicauca.validation.templatemethod;

import co.edu.unicauca.validation.domain.model.Question;

public class MultipleChoiceEvaluator extends QuestionEvaluator {
    @Override
    protected boolean isValidFormat(String answer) { return answer.matches("[A-Da-d]"); }

    @Override
    protected double calculateScore(Question question, String answer) {
        boolean correct = question.getCorrectOption()
                .map(o -> o.getLabel().equalsIgnoreCase(answer))
                .orElse(false);
        return correct ? BASE_MAX_SCORE : 0.0;
    }

    @Override
    protected String expectedFormat() { return "una letra entre A y D"; }

    @Override
    protected String buildFeedback(Question question, String answer, double score) {
        String correctLabel = question.getCorrectOption().map(o -> o.getLabel()).orElse("?");
        if (score >= BASE_MAX_SCORE) {
            return "Correcto. " + super.buildFeedback(question, answer, score);
        }
        return "Incorrecto. La respuesta correcta es " + correctLabel + ". "
                + super.buildFeedback(question, answer, score);
    }
}
