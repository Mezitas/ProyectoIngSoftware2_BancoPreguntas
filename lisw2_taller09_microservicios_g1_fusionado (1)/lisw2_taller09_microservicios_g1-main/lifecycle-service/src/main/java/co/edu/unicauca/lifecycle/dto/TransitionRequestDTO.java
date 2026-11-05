package co.edu.unicauca.lifecycle.dto;

import co.edu.unicauca.lifecycle.model.QuestionStatus;
import jakarta.validation.constraints.NotNull;

/** Solicitud de transición. {@code comment} es obligatorio cuando el destino es REJECTED (motivo). */
public record TransitionRequestDTO(
        @NotNull(message = "targetState es obligatorio") QuestionStatus targetState,
        Long requestedBy,
        String comment) {
}
