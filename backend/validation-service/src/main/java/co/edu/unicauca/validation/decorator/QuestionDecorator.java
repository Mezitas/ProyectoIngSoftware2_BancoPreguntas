package co.edu.unicauca.validation.decorator;

import co.edu.unicauca.validation.domain.model.Question;

public abstract class QuestionDecorator implements QuestionComponent {
    protected final QuestionComponent wrapped;

    protected QuestionDecorator(QuestionComponent wrapped) { this.wrapped = wrapped; }

    @Override public Question getQuestion() { return wrapped.getQuestion(); }
    @Override public String getDescription() { return wrapped.getDescription(); }
    @Override public double getMaxScore() { return wrapped.getMaxScore(); }
    @Override public String getExplanation() { return wrapped.getExplanation(); }
}
