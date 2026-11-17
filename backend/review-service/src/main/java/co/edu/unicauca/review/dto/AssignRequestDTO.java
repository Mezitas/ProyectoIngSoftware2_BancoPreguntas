package co.edu.unicauca.review.dto;

import jakarta.validation.constraints.NotNull;

public record AssignRequestDTO(
        @NotNull(message = "questionId es obligatorio") Long questionId,
        @NotNull(message = "reviewerId es obligatorio") Long reviewerId,
        Long assignedBy) {

    /** Copia con el administrador tomado del JWT (el cuerpo ya no es fuente de identidad). */
    public AssignRequestDTO withAssignedBy(Long administratorId) {
        return new AssignRequestDTO(questionId, reviewerId, administratorId);
    }
}
