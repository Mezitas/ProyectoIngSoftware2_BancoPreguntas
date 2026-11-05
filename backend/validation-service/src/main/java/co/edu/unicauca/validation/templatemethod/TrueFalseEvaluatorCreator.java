package co.edu.unicauca.validation.templatemethod;

import co.edu.unicauca.validation.dto.EvaluateRequestDTO;

public final class TrueFalseEvaluatorCreator extends QuestionEvaluatorCreator {
    @Override
    protected QuestionEvaluator create(EvaluateRequestDTO request) {
        if (request.expected() == null) {
            throw new IllegalArgumentException("expected (boolean) es obligatorio para TRUE_FALSE");
        }
        return new TrueFalseEvaluator(request.expected());
    }
}
