package co.edu.unicauca.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.unicauca.lifecycle.domain.QuestionLifecycle;
import co.edu.unicauca.lifecycle.exception.BadRequestException;
import co.edu.unicauca.lifecycle.exception.InvalidTransitionException;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import org.junit.jupiter.api.Test;

class QuestionLifecycleTest {

    @Test
    void aggregateAppliesOnlyLegalTransitions() {
        QuestionLifecycle lifecycle = QuestionLifecycle.draft(17L, 3L);

        lifecycle.transitionTo(QuestionStatus.PENDING_REVIEW, 3L, null);
        lifecycle.transitionTo(QuestionStatus.IN_REVIEW, 2L, null);

        assertEquals(QuestionStatus.IN_REVIEW, lifecycle.status());
        assertEquals(2L, lifecycle.changedBy());
        assertThrows(InvalidTransitionException.class,
                () -> lifecycle.transitionTo(QuestionStatus.PUBLISHED, 2L, null));
        assertEquals(QuestionStatus.IN_REVIEW, lifecycle.status());
    }

    @Test
    void rejectionRequiresAReasonAndDoesNotMutateTheAggregateWhenInvalid() {
        QuestionLifecycle lifecycle = QuestionLifecycle.draft(17L, 3L);
        lifecycle.transitionTo(QuestionStatus.PENDING_REVIEW, 3L, null);
        lifecycle.transitionTo(QuestionStatus.IN_REVIEW, 2L, null);

        assertThrows(BadRequestException.class,
                () -> lifecycle.transitionTo(QuestionStatus.REJECTED, 2L, " "));
        assertEquals(QuestionStatus.IN_REVIEW, lifecycle.status());
        lifecycle.transitionTo(QuestionStatus.REJECTED, 2L, "No cumple los criterios");
        assertEquals(QuestionStatus.REJECTED, lifecycle.status());
    }
}
