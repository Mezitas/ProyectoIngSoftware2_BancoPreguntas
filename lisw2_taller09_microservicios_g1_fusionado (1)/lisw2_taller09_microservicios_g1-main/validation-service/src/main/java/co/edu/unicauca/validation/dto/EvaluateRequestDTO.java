package co.edu.unicauca.validation.dto;

import java.util.List;

public record EvaluateRequestDTO(
        ValidationRequestDTO question,
        String answer,
        String type,
        Boolean expected,
        List<ValidationRequestDTO> subQuestions) {}
