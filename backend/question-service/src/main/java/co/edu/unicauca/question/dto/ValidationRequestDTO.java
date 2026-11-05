package co.edu.unicauca.question.dto;

import java.util.List;

public record ValidationRequestDTO(String context, String statement, List<OptionDTO> options, String correctOption,
                                   String justification, String competency, String topic, String subtopic,
                                   String bibliography, String difficulty) {
}
