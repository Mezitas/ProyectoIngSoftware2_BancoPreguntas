package co.edu.unicauca.lifecycle.state;

import co.edu.unicauca.lifecycle.exception.InvalidTransitionException;
import co.edu.unicauca.lifecycle.model.QuestionStatus;

/**
 * Patrón State (heredado del Taller 8). Cada estado concreto sobrescribe SOLO las
 * transiciones que le están permitidas; las demás heredan el rechazo por defecto.
 *
 * <pre>
 * DRAFT -> PENDING_REVIEW -> IN_REVIEW -> APPROVED -> PUBLISHED
 *   ^                           |  |
 *   |------ requestChanges -----|  +--> REJECTED --(corregir)--> DRAFT | PENDING_REVIEW
 * </pre>
 */
public interface IQuestionState {

    QuestionStatus status();

    default void submitForReview(LifecycleContext ctx) {
        throw new InvalidTransitionException(status(), QuestionStatus.PENDING_REVIEW);
    }

    default void startReview(LifecycleContext ctx) {
        throw new InvalidTransitionException(status(), QuestionStatus.IN_REVIEW);
    }

    default void approve(LifecycleContext ctx) {
        throw new InvalidTransitionException(status(), QuestionStatus.APPROVED);
    }

    default void reject(LifecycleContext ctx) {
        throw new InvalidTransitionException(status(), QuestionStatus.REJECTED);
    }

    /** Devuelve la pregunta a borrador (correcciones solicitadas o pregunta rechazada corregida). */
    default void returnToDraft(LifecycleContext ctx) {
        throw new InvalidTransitionException(status(), QuestionStatus.DRAFT);
    }

    default void publish(LifecycleContext ctx) {
        throw new InvalidTransitionException(status(), QuestionStatus.PUBLISHED);
    }
}
