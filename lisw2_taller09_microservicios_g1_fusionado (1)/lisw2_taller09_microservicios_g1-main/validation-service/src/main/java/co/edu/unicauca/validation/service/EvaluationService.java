package co.edu.unicauca.validation.service;

import co.edu.unicauca.validation.domain.model.Context;
import co.edu.unicauca.validation.domain.model.Option;
import co.edu.unicauca.validation.domain.model.Question;
import co.edu.unicauca.validation.dto.EvaluateRequestDTO;
import co.edu.unicauca.validation.dto.EvaluateResponseDTO;
import co.edu.unicauca.validation.dto.OptionDTO;
import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import co.edu.unicauca.validation.templatemethod.EvaluationResult;
import co.edu.unicauca.validation.templatemethod.QuestionEvaluator;
import co.edu.unicauca.validation.templatemethod.QuestionEvaluatorFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationService {
    public EvaluateResponseDTO evaluate(EvaluateRequestDTO req) {
        if (req == null || req.question() == null || req.type() == null || req.type().isBlank()) {
            throw new IllegalArgumentException("question y type son obligatorios");
        }

        QuestionEvaluator evaluator = switch (req.type().trim().toUpperCase()) {
            case "MULTIPLE_CHOICE" -> QuestionEvaluatorFactory.multipleChoice();
            case "TRUE_FALSE" -> {
                if (req.expected() == null) {
                    throw new IllegalArgumentException("expected (boolean) es obligatorio para TRUE_FALSE");
                }
                yield QuestionEvaluatorFactory.trueFalse(req.expected());
            }
            case "CASE_STUDY" -> {
                if (req.subQuestions() == null || req.subQuestions().isEmpty()) {
                    throw new IllegalArgumentException("subQuestions es obligatoria para CASE_STUDY");
                }
                yield QuestionEvaluatorFactory.caseStudy(
                        req.subQuestions().stream().map(this::toDomain).toList());
            }
            default -> throw new IllegalArgumentException("tipo de pregunta no soportado: " + req.type()
                    + " (use MULTIPLE_CHOICE, TRUE_FALSE o CASE_STUDY)");
        };

        EvaluationResult result = evaluator.evaluate(toDomain(req.question()), req.answer());
        return new EvaluateResponseDTO(result.isValidFormat(), result.getScore(), result.getMaxScore(), result.getFeedback());
    }

    private Question toDomain(ValidationRequestDTO q) {
        String correct = q.correctOption() == null ? "" : q.correctOption().trim().toUpperCase();
        List<Option> options = q.options() == null ? List.of() : q.options().stream()
                .map(o -> new Option(
                        o.label() == null ? "" : o.label().trim().toUpperCase(),
                        o.text(),
                        o.label() != null && o.label().trim().equalsIgnoreCase(correct)))
                .toList();

        return new Question(null, new Context(q.context()),
                q.statement() == null ? "" : q.statement(), options,
                q.justification(), q.competency(), q.topic(), q.subtopic());
    }
}
