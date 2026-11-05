package co.edu.unicauca.validation.templatemethod;

import co.edu.unicauca.validation.dto.EvaluateRequestDTO;

public final class MultipleChoiceEvaluatorCreator extends QuestionEvaluatorCreator {
    @Override
    protected QuestionEvaluator create(EvaluateRequestDTO request) {
        return new MultipleChoiceEvaluator();
    }
}
