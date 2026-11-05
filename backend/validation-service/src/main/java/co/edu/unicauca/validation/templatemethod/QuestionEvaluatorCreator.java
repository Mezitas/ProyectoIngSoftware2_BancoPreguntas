package co.edu.unicauca.validation.templatemethod;

import co.edu.unicauca.validation.domain.model.Context;
import co.edu.unicauca.validation.domain.model.Option;
import co.edu.unicauca.validation.domain.model.Question;
import co.edu.unicauca.validation.dto.EvaluateRequestDTO;
import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import java.util.List;

/** Factory Method: concrete creators choose the evaluator product for each request type. */
public abstract class QuestionEvaluatorCreator {

    public final QuestionEvaluator createEvaluator(EvaluateRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de evaluación es obligatoria");
        }
        return create(request);
    }

    protected abstract QuestionEvaluator create(EvaluateRequestDTO request);

    protected static Question toDomain(ValidationRequestDTO request) {
        String correct = request.correctOption() == null ? "" : request.correctOption().trim().toUpperCase();
        List<Option> options = request.options() == null ? List.of() : request.options().stream()
                .map(option -> new Option(
                        option.label() == null ? "" : option.label().trim().toUpperCase(),
                        option.text(),
                        option.label() != null && option.label().trim().equalsIgnoreCase(correct)))
                .toList();

        return new Question(null, new Context(request.context()),
                request.statement() == null ? "" : request.statement(), options,
                request.justification(), request.competency(), request.topic(), request.subtopic());
    }
}
