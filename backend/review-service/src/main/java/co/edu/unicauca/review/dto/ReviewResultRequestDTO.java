package co.edu.unicauca.review.dto;

import co.edu.unicauca.review.model.ReviewResult;
import jakarta.validation.constraints.NotNull;

/** Resultado de la revisión. {@code comments} es obligatorio si result = REJECTED (motivo). */
public record ReviewResultRequestDTO(
        @NotNull(message = "questionId es obligatorio") Long questionId,
        Long reviewerId,
        @NotNull(message = "result es obligatorio (APPROVED o REJECTED)") ReviewResult result,
        String comments) {

    /** Copia con el revisor tomado del JWT (el cuerpo ya no es fuente de identidad). */
    public ReviewResultRequestDTO withReviewerId(Long authenticatedReviewerId) {
        return new ReviewResultRequestDTO(questionId, authenticatedReviewerId, result, comments);
    }
}
