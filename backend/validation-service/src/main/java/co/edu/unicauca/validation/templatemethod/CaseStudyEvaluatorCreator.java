package co.edu.unicauca.validation.templatemethod;

import co.edu.unicauca.validation.dto.EvaluateRequestDTO;

public final class CaseStudyEvaluatorCreator extends QuestionEvaluatorCreator {
    @Override
    protected QuestionEvaluator create(EvaluateRequestDTO request) {
        if (request.subQuestions() == null || request.subQuestions().isEmpty()) {
            throw new IllegalArgumentException("subQuestions es obligatoria para CASE_STUDY");
        }
        return new CaseStudyEvaluator(request.subQuestions().stream()
                .map(QuestionEvaluatorCreator::toDomain)
                .toList());
    }
}
