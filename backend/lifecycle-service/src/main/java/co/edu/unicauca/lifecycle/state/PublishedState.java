package co.edu.unicauca.lifecycle.state;

import co.edu.unicauca.lifecycle.model.QuestionStatus;

public class PublishedState implements IQuestionState {

    @Override
    public QuestionStatus status() {
        return QuestionStatus.PUBLISHED;
    }
}
