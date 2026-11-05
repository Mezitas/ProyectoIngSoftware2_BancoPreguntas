package co.edu.unicauca.validation.templatemethod;

import co.edu.unicauca.validation.domain.model.Question;

public class TrueFalseEvaluator extends QuestionEvaluator {
    private final boolean expectedValue;

    public TrueFalseEvaluator(boolean expectedValue) { this.expectedValue = expectedValue; }

    @Override
    protected boolean isValidFormat(String answer) {
        return answer.matches("(?i)(V|F|VERDADERO|FALSO|TRUE|FALSE)");
    }

    @Override
    protected double calculateScore(Question question, String answer) {
        return parse(answer) == expectedValue ? BASE_MAX_SCORE : 0.0;
    }

    @Override
    protected String expectedFormat() { return "V o F (también VERDADERO/FALSO o TRUE/FALSE)"; }

    private boolean parse(String answer) {
        return answer.matches("(?i)(V|VERDADERO|TRUE)");
    }
}
