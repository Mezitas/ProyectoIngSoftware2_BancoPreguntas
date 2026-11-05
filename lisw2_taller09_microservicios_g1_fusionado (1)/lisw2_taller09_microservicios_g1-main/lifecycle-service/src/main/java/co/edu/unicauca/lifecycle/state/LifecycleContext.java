package co.edu.unicauca.lifecycle.state;

import co.edu.unicauca.lifecycle.model.QuestionStatus;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Contexto del patrón State: conoce el estado actual y le delega cada operación.
 * No contiene condicionales: tanto la reconstrucción del estado desde su valor
 * persistido como la traducción "estado destino -> operación" se resuelven con tablas.
 */
public class LifecycleContext {

    private static final Map<QuestionStatus, Supplier<IQuestionState>> FACTORY = new EnumMap<>(QuestionStatus.class);
    private static final Map<QuestionStatus, Action> ACTIONS = new EnumMap<>(QuestionStatus.class);

    @FunctionalInterface
    private interface Action {
        void run(LifecycleContext ctx);
    }

    static {
        FACTORY.put(QuestionStatus.DRAFT, DraftState::new);
        FACTORY.put(QuestionStatus.PENDING_REVIEW, PendingReviewState::new);
        FACTORY.put(QuestionStatus.IN_REVIEW, InReviewState::new);
        FACTORY.put(QuestionStatus.APPROVED, ApprovedState::new);
        FACTORY.put(QuestionStatus.REJECTED, RejectedState::new);
        FACTORY.put(QuestionStatus.PUBLISHED, PublishedState::new);

        ACTIONS.put(QuestionStatus.PENDING_REVIEW, LifecycleContext::submitForReview);
        ACTIONS.put(QuestionStatus.IN_REVIEW, LifecycleContext::startReview);
        ACTIONS.put(QuestionStatus.APPROVED, LifecycleContext::approve);
        ACTIONS.put(QuestionStatus.REJECTED, LifecycleContext::reject);
        ACTIONS.put(QuestionStatus.DRAFT, LifecycleContext::returnToDraft);
        ACTIONS.put(QuestionStatus.PUBLISHED, LifecycleContext::publish);
    }

    private IQuestionState state;

    private LifecycleContext(IQuestionState state) {
        this.state = state;
    }

    /** Reconstruye el contexto a partir del estado persistido. */
    public static LifecycleContext of(QuestionStatus status) {
        return new LifecycleContext(FACTORY.get(status).get());
    }

    public QuestionStatus getStatus() {
        return state.status();
    }

    /** Solicita pasar al estado destino; lanza InvalidTransitionException si no es válido. */
    public void transitionTo(QuestionStatus target) {
        ACTIONS.get(target).run(this);
    }

    public void submitForReview() { state.submitForReview(this); }
    public void startReview() { state.startReview(this); }
    public void approve() { state.approve(this); }
    public void reject() { state.reject(this); }
    public void returnToDraft() { state.returnToDraft(this); }
    public void publish() { state.publish(this); }

    void changeState(IQuestionState newState) {
        this.state = newState;
    }
}
