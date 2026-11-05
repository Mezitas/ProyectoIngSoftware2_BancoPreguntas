package co.edu.unicauca.question.decorator;

/** Decorador concreto: marca la pregunta como de alta dificultad y multiplica su puntaje. */
public class HighDifficultyQuestion extends QuestionDecorator {
    public static final double DEFAULT_MULTIPLIER = 1.5;
    private final double multiplier;

    public HighDifficultyQuestion(QuestionComponent wrapped) { this(wrapped, DEFAULT_MULTIPLIER); }
    public HighDifficultyQuestion(QuestionComponent wrapped, double multiplier) {
        super(wrapped);
        this.multiplier = multiplier;
    }

    public double getMultiplier() { return multiplier; }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + "  [ALTA DIFICULTAD x" + multiplier + "]";
    }

    @Override
    public double getMaxScore() { return wrapped.getMaxScore() * multiplier; }
}
