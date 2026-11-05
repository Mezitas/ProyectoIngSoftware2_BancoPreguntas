package co.edu.unicauca.lifecycle.state;

import co.edu.unicauca.lifecycle.model.QuestionStatus;

public class PendingReviewState implements IQuestionState {

    @Override
    public QuestionStatus status() {
        return QuestionStatus.PENDING_REVIEW;
    }

    @Override
    public void startReview(LifecycleContext ctx) {
        ctx.changeState(new InReviewState());
    }
}
