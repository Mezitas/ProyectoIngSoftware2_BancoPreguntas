package co.edu.unicauca.validation.decorator;

import co.edu.unicauca.validation.domain.model.Question;

public class BasicQuestion implements QuestionComponent {
    private final Question question;

    public BasicQuestion(Question question) { this.question = question; }

    @Override public Question getQuestion() { return question; }
    @Override public String getDescription() {
        return question.getTopic() + " / " + question.getSubtopic() + " — " + question.getStatement();
    }
    @Override public double getMaxScore() { return 1.0; }
    @Override public String getExplanation() { return question.getJustification(); }
}
