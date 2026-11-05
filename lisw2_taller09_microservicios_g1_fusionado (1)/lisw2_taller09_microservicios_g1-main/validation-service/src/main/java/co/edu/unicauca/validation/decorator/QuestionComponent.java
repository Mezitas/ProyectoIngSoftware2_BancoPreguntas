package co.edu.unicauca.validation.decorator;

import co.edu.unicauca.validation.domain.model.Question;

public interface QuestionComponent {
    Question getQuestion();
    String getDescription();
    double getMaxScore();
    String getExplanation();
}
