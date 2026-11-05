package co.edu.unicauca.validation.dto;

import java.util.List;

public record ValidationResultDTO(boolean valid, List<String> errors) {

    public static ValidationResultDTO of(List<String> errors) {
        return new ValidationResultDTO(errors.isEmpty(), errors);
    }
}
