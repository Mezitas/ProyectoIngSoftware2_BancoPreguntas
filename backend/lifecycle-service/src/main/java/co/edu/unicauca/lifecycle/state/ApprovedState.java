package co.edu.unicauca.lifecycle.state;

import co.edu.unicauca.lifecycle.model.QuestionStatus;

public class ApprovedState implements IQuestionState {

    @Override
    public QuestionStatus status() {
        return QuestionStatus.APPROVED;
    }

    @Override
    public void publish(LifecycleContext ctx) {
        ctx.changeState(new PublishedState());
    }
}
