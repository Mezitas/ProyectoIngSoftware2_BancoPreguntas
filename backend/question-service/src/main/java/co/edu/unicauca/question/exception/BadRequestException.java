package co.edu.unicauca.question.exception;

/** Parámetro o petición inválida (HTTP 400). */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
