package co.edu.unicauca.lifecycle;

import static org.junit.jupiter.api.Assertions.*;

import co.edu.unicauca.lifecycle.exception.InvalidTransitionException;
import co.edu.unicauca.lifecycle.model.QuestionStatus;
import co.edu.unicauca.lifecycle.state.LifecycleContext;
import org.junit.jupiter.api.Test;

class LifecycleContextTest {

    private static LifecycleContext in(QuestionStatus s) {
        return LifecycleContext.of(s);
    }

    @Test
    void flujoCompletoHastaPublicada() {
        LifecycleContext c = in(QuestionStatus.DRAFT);
        c.transitionTo(QuestionStatus.PENDING_REVIEW);
        c.transitionTo(QuestionStatus.IN_REVIEW);
        c.transitionTo(QuestionStatus.APPROVED);
        c.transitionTo(QuestionStatus.PUBLISHED);
        assertEquals(QuestionStatus.PUBLISHED, c.getStatus());
    }

    @Test
    void aprobarUnBorradorSeRechaza() {
        LifecycleContext c = in(QuestionStatus.DRAFT);
        InvalidTransitionException e = assertThrows(InvalidTransitionException.class,
                () -> c.transitionTo(QuestionStatus.APPROVED));
        assertTrue(e.getMessage().contains("DRAFT"));
        assertEquals(QuestionStatus.DRAFT, c.getStatus());
    }

    @Test
    void enRevisionPuedeRechazarse() {
        LifecycleContext c = in(QuestionStatus.IN_REVIEW);
        c.transitionTo(QuestionStatus.REJECTED);
        assertEquals(QuestionStatus.REJECTED, c.getStatus());
    }

    @Test
    void rechazadaPuedeVolverABorradorYAReenviarse() {
        LifecycleContext c = in(QuestionStatus.REJECTED);
        c.transitionTo(QuestionStatus.DRAFT);
        c.transitionTo(QuestionStatus.PENDING_REVIEW);
        assertEquals(QuestionStatus.PENDING_REVIEW, c.getStatus());
    }

    @Test
    void enRevisionPuedeDevolverseABorrador() {
        LifecycleContext c = in(QuestionStatus.IN_REVIEW);
        c.transitionTo(QuestionStatus.DRAFT);
        assertEquals(QuestionStatus.DRAFT, c.getStatus());
    }

    @Test
    void publicadaEsEstadoFinal() {
        for (QuestionStatus target : QuestionStatus.values()) {
            LifecycleContext c = in(QuestionStatus.PUBLISHED);
            assertThrows(InvalidTransitionException.class, () -> c.transitionTo(target));
        }
    }

    @Test
    void nadaSePuedeSaltar() {
        assertThrows(InvalidTransitionException.class, () -> in(QuestionStatus.DRAFT).transitionTo(QuestionStatus.IN_REVIEW));
        assertThrows(InvalidTransitionException.class, () -> in(QuestionStatus.DRAFT).transitionTo(QuestionStatus.PUBLISHED));
        assertThrows(InvalidTransitionException.class, () -> in(QuestionStatus.PENDING_REVIEW).transitionTo(QuestionStatus.APPROVED));
        assertThrows(InvalidTransitionException.class, () -> in(QuestionStatus.APPROVED).transitionTo(QuestionStatus.REJECTED));
    }
}
