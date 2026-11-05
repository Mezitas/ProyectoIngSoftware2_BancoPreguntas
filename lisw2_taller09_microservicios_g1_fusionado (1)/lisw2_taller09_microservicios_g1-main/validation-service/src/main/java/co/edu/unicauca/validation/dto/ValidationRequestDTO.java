package co.edu.unicauca.validation.dto;

import java.util.List;

/** Contenido y metadatos estructurales requeridos para una pregunta del banco. */
public record ValidationRequestDTO(
        String context,
        String statement,
        List<OptionDTO> options,
        String correctOption,
        String justification,
        String competency,
        String topic,
        String subtopic,
        String bibliography,
        String difficulty) {
}
