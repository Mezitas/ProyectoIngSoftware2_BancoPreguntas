package co.edu.unicauca.validation.service;

import co.edu.unicauca.validation.dto.OptionDTO;
import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import co.edu.unicauca.validation.dto.ValidationResultDTO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Reglas de calidad de una pregunta Saber PRO. Reúne las reglas que en el Taller 8
 * estaban repartidas entre QuestionBuilder (estructura: enunciado, 4 opciones,
 * 1 respuesta correcta) y QuestionValidator (contexto, justificación, clasificación,
 * opciones no repetidas). Devuelve TODOS los errores encontrados, no solo el primero.
 */
@Service
public class ValidationService {

    static final int REQUIRED_OPTIONS = 4;
    static final Set<String> VALID_LABELS = Set.of("A", "B", "C", "D");

    public ValidationResultDTO validate(ValidationRequestDTO q) {
        List<String> errors = new ArrayList<>();

        if (isBlank(q.statement())) {
            errors.add("la pregunta debe tener enunciado");
        }
        if (isBlank(q.context())) {
            errors.add("falta el contexto");
        }

        validateOptions(q, errors);

        if (isBlank(q.justification())) {
            errors.add("falta la justificación");
        }
        if (isBlank(q.competency()) || isBlank(q.topic()) || isBlank(q.subtopic())) {
            errors.add("falta la clasificación (competencia, tema y subtema)");
        }
        if (isBlank(q.bibliography())) {
            errors.add("falta la bibliografía");
        }
        if (!isValidDifficulty(q.difficulty())) {
            errors.add("el nivel de dificultad debe ser BASIC, MEDIUM o HIGH");
        }
        return ValidationResultDTO.of(errors);
    }

    private static boolean isValidDifficulty(String difficulty) {
        if (difficulty == null) {
            return false;
        }
        return switch (difficulty.trim().toUpperCase(java.util.Locale.ROOT)) {
            case "BASIC", "BASICA", "BÁSICA", "MEDIUM", "MEDIA", "HIGH", "ALTA" -> true;
            default -> false;
        };
    }

    private void validateOptions(ValidationRequestDTO q, List<String> errors) {
        List<OptionDTO> options = q.options() == null ? List.of() : q.options();
        if (options.size() != REQUIRED_OPTIONS) {
            errors.add("debe tener exactamente 4 opciones (se recibieron " + options.size() + ")");
        }

        Set<String> labels = new HashSet<>();
        Set<String> texts = new HashSet<>();
        boolean repeatedText = false;
        for (OptionDTO option : options) {
            String label = option == null || option.label() == null ? "" : option.label().trim().toUpperCase();
            if (!VALID_LABELS.contains(label)) {
                errors.add("etiqueta de opción inválida: '" + label + "' (use A, B, C o D)");
            } else if (!labels.add(label)) {
                errors.add("la etiqueta '" + label + "' está repetida");
            }
            if (option == null || isBlank(option.text())) {
                errors.add("la opción " + label + " está vacía");
            } else if (!texts.add(option.text().trim().toLowerCase())) {
                repeatedText = true;
            }
        }
        if (repeatedText) {
            errors.add("hay opciones repetidas");
        }

        if (isBlank(q.correctOption())) {
            errors.add("debe tener exactamente 1 respuesta correcta (correctOption no indicada)");
        } else if (!labels.contains(q.correctOption().trim().toUpperCase())) {
            errors.add("la respuesta correcta '" + q.correctOption() + "' no corresponde a ninguna opción");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
