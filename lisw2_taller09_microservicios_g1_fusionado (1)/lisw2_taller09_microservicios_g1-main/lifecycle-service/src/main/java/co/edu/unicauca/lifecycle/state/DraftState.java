package co.edu.unicauca.lifecycle.state;

import co.edu.unicauca.lifecycle.model.QuestionStatus;

public class DraftState implements IQuestionState {

    @Override
    public QuestionStatus status() {
        return QuestionStatus.DRAFT;
    }

    @Override
    public void submitForReview(LifecycleContext ctx) {
        ctx.changeState(new PendingReviewState());
    }
}
