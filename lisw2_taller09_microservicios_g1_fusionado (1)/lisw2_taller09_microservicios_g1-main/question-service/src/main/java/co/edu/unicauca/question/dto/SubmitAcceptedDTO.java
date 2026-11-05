package co.edu.unicauca.question.dto;

/** Respuesta 202 Accepted: confirma la aceptación, no que la transición haya terminado. */
public record SubmitAcceptedDTO(String message, String operationId, Long questionId, String status,
                                String statusUrl) {
}
