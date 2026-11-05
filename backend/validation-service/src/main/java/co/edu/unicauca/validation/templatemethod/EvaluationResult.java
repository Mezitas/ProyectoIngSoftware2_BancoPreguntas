package co.edu.unicauca.validation.templatemethod;

/** Resultado inmutable de evaluar una respuesta. */
public class EvaluationResult {
    private final boolean validFormat;
    private final double score;
    private final double maxScore;
    private final String feedback;

    public EvaluationResult(boolean validFormat, double score, double maxScore, String feedback) {
        this.validFormat = validFormat;
        this.score = score;
        this.maxScore = maxScore;
        this.feedback = feedback;
    }

    public boolean isValidFormat() { return validFormat; }
    public double getScore() { return score; }
    public double getMaxScore() { return maxScore; }
    public String getFeedback() { return feedback; }

    public EvaluationResult scaledBy(double factor) {
        return new EvaluationResult(validFormat, score * factor, maxScore * factor, feedback);
    }

    @Override
    public String toString() {
        return String.format("EvaluationResult{validFormat=%s, score=%.2f/%.2f, feedback=%s}",
                validFormat, score, maxScore, feedback);
    }
}
