package co.edu.unicauca.lifecycle.state;

import co.edu.unicauca.lifecycle.model.QuestionStatus;

public class InReviewState implements IQuestionState {

    @Override
    public QuestionStatus status() {
        return QuestionStatus.IN_REVIEW;
    }

    @Override
    public void approve(LifecycleContext ctx) {
        ctx.changeState(new ApprovedState());
    }

    @Override
    public void reject(LifecycleContext ctx) {
        ctx.changeState(new RejectedState());
    }

    @Override
    public void returnToDraft(LifecycleContext ctx) {
        ctx.changeState(new DraftState());
    }
}
