package co.edu.unicauca.validation.templatemethod;

import co.edu.unicauca.validation.domain.model.Question;
import java.util.List;

public final class QuestionEvaluatorFactory {
    private QuestionEvaluatorFactory() {}

    public static QuestionEvaluator multipleChoice() { return new MultipleChoiceEvaluator(); }
    public static QuestionEvaluator trueFalse(boolean expectedValue) { return new TrueFalseEvaluator(expectedValue); }
    public static QuestionEvaluator caseStudy(List<Question> subQuestions) {
        return new CaseStudyEvaluator(subQuestions);
    }
}
