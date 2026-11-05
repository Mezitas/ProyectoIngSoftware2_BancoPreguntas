package co.edu.unicauca.validation.dto;

public record EvaluateResponseDTO(boolean validFormat, double score, double maxScore, String feedback) {}
