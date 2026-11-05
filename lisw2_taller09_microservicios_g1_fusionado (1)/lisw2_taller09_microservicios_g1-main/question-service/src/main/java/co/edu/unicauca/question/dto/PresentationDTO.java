package co.edu.unicauca.question.dto;

/** Salida del endpoint de presentación decorada (patrón Decorator). */
public record PresentationDTO(String description, String explanation, double maxScore) {}
