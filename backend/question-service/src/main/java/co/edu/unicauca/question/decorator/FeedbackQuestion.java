package co.edu.unicauca.question.decorator;

/** Decorador concreto: adjunta una explicación técnica detallada a la pregunta. */
public class FeedbackQuestion extends QuestionDecorator {
    private final String technicalExplanation;

    public FeedbackQuestion(QuestionComponent wrapped, String technicalExplanation) {
        super(wrapped);
        this.technicalExplanation = technicalExplanation == null ? "" : technicalExplanation.trim();
    }

    public String getTechnicalExplanation() { return technicalExplanation; }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + "  [con retroalimentación técnica]";
    }

    @Override
    public String getExplanation() {
        return wrapped.getExplanation() + "\nRetroalimentación técnica: " + technicalExplanation;
    }
}
