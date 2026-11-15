package co.edu.unicauca.lifecycle.domain;

import co.edu.unicauca.lifecycle.exception.BadRequestException;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import co.edu.unicauca.lifecycle.state.LifecycleContext;
import java.time.Instant;

/**
 * Aggregate root for the question lifecycle. It owns transition rules and contains no
 * persistence or transport types; adapters reconstruct it from storage.
 */
public final class QuestionLifecycle {

    private final Long questionId;
    private final Long createdBy;
    private QuestionStatus status;
    private Long changedBy;
    private String comment;
    private Instant changedAt;

    private QuestionLifecycle(Long questionId, QuestionStatus status, Long createdBy, Long changedBy,
                              String comment, Instant changedAt) {
        this.questionId = questionId;
        this.status = status;
        this.createdBy = createdBy;
        this.changedBy = changedBy;
        this.comment = comment;
        this.changedAt = changedAt;
    }

    /** Creates the initial aggregate in DRAFT. */
    public static QuestionLifecycle draft(Long questionId, Long createdBy) {
        return new QuestionLifecycle(questionId, QuestionStatus.DRAFT, createdBy, createdBy,
                "Pregunta creada", Instant.now());
    }

    /** Reconstitutes an aggregate from its stored state. */
    public static QuestionLifecycle reconstitute(Long questionId, QuestionStatus status, Long createdBy,
                                                  Long changedBy, String comment, Instant changedAt) {
        return new QuestionLifecycle(questionId, status, createdBy, changedBy, comment, changedAt);
    }

    /**
     * Applies one permitted transition and records who requested it.
     *
     * @throws BadRequestException if a rejection has no reason
     */
    public void transitionTo(QuestionStatus target, Long requestedBy, String reason) {
        if (target == QuestionStatus.REJECTED && (reason == null || reason.isBlank())) {
            throw new BadRequestException("Para rechazar una pregunta debe indicar el motivo (comment)");
        }
        LifecycleContext context = LifecycleContext.of(status);
        context.transitionTo(target);
        status = context.getStatus();
        changedBy = requestedBy;
        comment = reason;
        changedAt = Instant.now();
    }

    public Long questionId() { return questionId; }
    public QuestionStatus status() { return status; }
    public Long createdBy() { return createdBy; }
    public Long changedBy() { return changedBy; }
    public String comment() { return comment; }
    public Instant changedAt() { return changedAt; }
}
