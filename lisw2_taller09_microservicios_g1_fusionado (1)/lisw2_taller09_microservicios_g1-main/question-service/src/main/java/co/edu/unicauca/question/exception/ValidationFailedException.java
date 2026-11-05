package co.edu.unicauca.question.exception;

import java.util.List;

/** La pregunta no cumple las reglas del Validation Service (HTTP 400). */
public class ValidationFailedException extends RuntimeException {

    private final List<String> errors;

    public ValidationFailedException(List<String> errors) {
        super("La pregunta no es válida");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
