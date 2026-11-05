package co.edu.unicauca.validation.templatemethod;

import co.edu.unicauca.validation.domain.model.Question;
import java.util.List;

public class CaseStudyEvaluator extends QuestionEvaluator {
    private final List<Question> subQuestions;

    public CaseStudyEvaluator(List<Question> subQuestions) { this.subQuestions = List.copyOf(subQuestions); }

    public int getSubQuestionCount() { return subQuestions.size(); }

    @Override
    protected boolean isValidFormat(String answer) {
        String[] parts = answer.split(",");
        if (parts.length != subQuestions.size()) return false;
        for (String p : parts) {
            if (!p.trim().matches("[A-Da-d]")) return false;
        }
        return true;
    }

    @Override
    protected double calculateScore(Question question, String answer) {
        String[] parts = answer.split(",");
        int correct = 0;
        for (int i = 0; i < subQuestions.size(); i++) {
            String expected = subQuestions.get(i).getCorrectOption().map(o -> o.getLabel()).orElse("");
            if (expected.equalsIgnoreCase(parts[i].trim())) correct++;
        }
        return subQuestions.isEmpty() ? 0.0 : BASE_MAX_SCORE * correct / subQuestions.size();
    }

    @Override
    protected String expectedFormat() {
        return subQuestions.size() + " letras entre A y D separadas por comas (p. ej. A,C,D)";
    }

    @Override
    protected String buildFeedback(Question question, String answer, double score) {
        StringBuilder sb = new StringBuilder();
        String[] parts = answer.split(",");
        for (int i = 0; i < subQuestions.size(); i++) {
            String expected = subQuestions.get(i).getCorrectOption().map(o -> o.getLabel()).orElse("?");
            sb.append("Sub-pregunta ").append(i + 1).append(": ")
              .append(expected.equalsIgnoreCase(parts[i].trim()) ? "correcta" : "incorrecta (era " + expected + ")")
              .append(". ");
        }
        return sb.toString();
    }
}
