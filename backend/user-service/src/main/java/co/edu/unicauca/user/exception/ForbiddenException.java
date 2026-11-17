package co.edu.unicauca.user.exception;

/** El usuario autenticado no tiene permiso para la operación solicitada (HTTP 403). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
