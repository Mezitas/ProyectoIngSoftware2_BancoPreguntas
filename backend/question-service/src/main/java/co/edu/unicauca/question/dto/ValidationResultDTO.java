package co.edu.unicauca.question.dto;

import java.util.List;

public record ValidationResultDTO(boolean valid, List<String> errors) {
}
