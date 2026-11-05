package co.edu.unicauca.lifecycle.exception;

import co.edu.unicauca.lifecycle.model.QuestionStatus;

/** La transición solicitada no es válida desde el estado actual (HTTP 409). */
public class InvalidTransitionException extends RuntimeException {

    public InvalidTransitionException(QuestionStatus from, QuestionStatus to) {
        super("Transición no permitida: " + from + " → " + to);
    }
}
