package co.edu.unicauca.lifecycle.state;

import co.edu.unicauca.lifecycle.model.QuestionStatus;

public class RejectedState implements IQuestionState {

    @Override
    public QuestionStatus status() {
        return QuestionStatus.REJECTED;
    }

    @Override
    public void returnToDraft(LifecycleContext ctx) {
        ctx.changeState(new DraftState());
    }

    @Override
    public void submitForReview(LifecycleContext ctx) {
        ctx.changeState(new PendingReviewState());
    }
}
