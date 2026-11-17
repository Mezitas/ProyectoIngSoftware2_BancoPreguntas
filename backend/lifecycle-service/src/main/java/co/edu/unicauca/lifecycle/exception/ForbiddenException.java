package co.edu.unicauca.lifecycle.exception;

/** El rol del usuario no permite la operación solicitada (HTTP 403). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
