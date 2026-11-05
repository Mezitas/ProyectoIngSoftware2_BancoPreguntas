package co.edu.unicauca.question;

import co.edu.unicauca.question.builder.QuestionBuilder;
import co.edu.unicauca.question.builder.QuestionDirector;
import co.edu.unicauca.question.decorator.*;
import co.edu.unicauca.question.domain.model.Question;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuestionDecoratorTest {
    private Question sample() { return new QuestionDirector(new QuestionBuilder()).buildSampleQuestion(); }

    @Test
    void basicaTienePuntaje1YJustificacion() {
        QuestionComponent c = new BasicQuestion(sample());
        assertEquals(1.0, c.getMaxScore());
        assertTrue(c.getExplanation().contains("Builder"));
    }

    @Test
    void altaDificultadMultiplicaPuntaje() {
        QuestionComponent c = new HighDifficultyQuestion(new BasicQuestion(sample()));
        assertEquals(1.5, c.getMaxScore(), 1e-9);
        assertTrue(c.getDescription().contains("ALTA DIFICULTAD"));
    }

    @Test
    void multiplicadorPersonalizado() {
        QuestionComponent c = new HighDifficultyQuestion(new BasicQuestion(sample()), 2.0);
        assertEquals(2.0, c.getMaxScore(), 1e-9);
    }

    @Test
    void feedbackAgregaExplicacionTecnica() {
        QuestionComponent c = new FeedbackQuestion(new BasicQuestion(sample()), "Detalle técnico aquí");
        assertTrue(c.getExplanation().contains("Detalle técnico aquí"));
        assertTrue(c.getDescription().contains("retroalimentación"));
    }

    @Test
    void decoradoresApilables() {
        QuestionComponent c = new FeedbackQuestion(
                new HighDifficultyQuestion(new BasicQuestion(sample())), "extra");
        assertEquals(1.5, c.getMaxScore(), 1e-9);
        assertTrue(c.getExplanation().contains("extra"));
        assertTrue(c.getDescription().contains("ALTA DIFICULTAD"));
    }

    @Test
    void decoradorNoModificaLaPreguntaOriginal() {
        Question q = sample();
        new HighDifficultyQuestion(new FeedbackQuestion(new BasicQuestion(q), "x"));
        assertEquals(4, q.getOptions().size());
        assertEquals(1, q.getOptions().stream().filter(o -> o.isCorrect()).count());
    }
}
