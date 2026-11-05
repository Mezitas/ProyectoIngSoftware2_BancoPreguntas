package co.edu.unicauca.validation;

import co.edu.unicauca.validation.domain.model.Context;
import co.edu.unicauca.validation.domain.model.Option;
import co.edu.unicauca.validation.domain.model.Question;
import co.edu.unicauca.validation.decorator.BasicQuestion;
import co.edu.unicauca.validation.decorator.HighDifficultyQuestion;
import co.edu.unicauca.validation.dto.EvaluateRequestDTO;
import co.edu.unicauca.validation.dto.ValidationRequestDTO;
import co.edu.unicauca.validation.templatemethod.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuestionEvaluatorTest {
    private Question mcq() {
        return new Question("q1", new Context("c"), "¿2+2?",
                List.of(new Option("A", "3", false), new Option("B", "4", true),
                        new Option("C", "5", false), new Option("D", "6", false)),
                "Porque 2+2=4.", "Comp", "Tema", "Sub");
    }

    @Test
    void multipleChoiceCorrectaDaPuntajeTotal() {
        EvaluationResult r = new MultipleChoiceEvaluator().evaluate(mcq(), "b");
        assertTrue(r.isValidFormat());
        assertEquals(1.0, r.getScore(), 1e-9);
        assertTrue(r.getFeedback().contains("Correcto"));
    }

    @Test
    void multipleChoiceIncorrectaDaCero() {
        EvaluationResult r = new MultipleChoiceEvaluator().evaluate(mcq(), "A");
        assertEquals(0.0, r.getScore(), 1e-9);
        assertTrue(r.getFeedback().contains("B"));
    }

    @Test
    void multipleChoiceRechazaFormatoInvalido() {
        assertFalse(new MultipleChoiceEvaluator().evaluate(mcq(), "E").isValidFormat());
        assertFalse(new MultipleChoiceEvaluator().evaluate(mcq(), "AB").isValidFormat());
        assertFalse(new MultipleChoiceEvaluator().evaluate(mcq(), " ").isValidFormat());
    }

    @Test
    void trueFalseAceptaSinonimos() {
        TrueFalseEvaluator ev = new TrueFalseEvaluator(true);
        assertEquals(1.0, ev.evaluate(mcq(), "V").getScore(), 1e-9);
        assertEquals(1.0, ev.evaluate(mcq(), "verdadero").getScore(), 1e-9);
        assertEquals(1.0, ev.evaluate(mcq(), "TRUE").getScore(), 1e-9);
        assertEquals(0.0, ev.evaluate(mcq(), "F").getScore(), 1e-9);
    }

    @Test
    void trueFalseRechazaFormatoInvalido() {
        assertFalse(new TrueFalseEvaluator(false).evaluate(mcq(), "quizás").isValidFormat());
    }

    @Test
    void caseStudyDaPuntajeParcial() {
        List<Question> subs = List.of(mcq(),
                new Question("q2", new Context("c"), "¿3+3?",
                        List.of(new Option("A", "5", false), new Option("B", "6", true),
                                new Option("C", "7", false), new Option("D", "8", false)),
                        "j", "c", "t", "s"),
                new Question("q3", new Context("c"), "¿5+5?",
                        List.of(new Option("A", "9", false), new Option("B", "11", false),
                                new Option("C", "10", true), new Option("D", "12", false)),
                        "j", "c", "t", "s"));
        CaseStudyEvaluator ev = new CaseStudyEvaluator(subs);
        EvaluationResult r = ev.evaluate(mcq(), "B,B,X");
        assertFalse(r.isValidFormat());

        EvaluationResult ok = ev.evaluate(mcq(), "B,A,C");
        assertTrue(ok.isValidFormat());
        assertEquals(2.0 / 3.0, ok.getScore(), 1e-9);
    }

    @Test
    void evaluacionEscaladaPorDecorador() {
        var component = new HighDifficultyQuestion(new BasicQuestion(mcq()));
        EvaluationResult r = new MultipleChoiceEvaluator().evaluate(component, "B");
        assertEquals(1.5, r.getScore(), 1e-9);
        assertEquals(1.5, r.getMaxScore(), 1e-9);
    }

    @Test
    void factoryMethodCreaElEvaluadorSegunElTipo() {
        ValidationRequestDTO question = new ValidationRequestDTO(
                "context", "statement", List.of(), "A", "justification",
                "competency", "topic", "subtopic", "bibliography", "MEDIUM");

        assertInstanceOf(MultipleChoiceEvaluator.class,
                new MultipleChoiceEvaluatorCreator().createEvaluator(
                        new EvaluateRequestDTO(question, "A", "MULTIPLE_CHOICE", null, null)));
        assertInstanceOf(TrueFalseEvaluator.class,
                new TrueFalseEvaluatorCreator().createEvaluator(
                        new EvaluateRequestDTO(question, "V", "TRUE_FALSE", true, null)));
        assertInstanceOf(CaseStudyEvaluator.class,
                new CaseStudyEvaluatorCreator().createEvaluator(
                        new EvaluateRequestDTO(question, "A", "CASE_STUDY", null, List.of(question))));
    }

    @Test
    void factoryMethodValidaLosDatosNecesariosParaCrearElEvaluador() {
        var trueFalseCreator = new TrueFalseEvaluatorCreator();
        var caseStudyCreator = new CaseStudyEvaluatorCreator();

        assertThrows(IllegalArgumentException.class, () -> trueFalseCreator.createEvaluator(
                new EvaluateRequestDTO(null, "V", "TRUE_FALSE", null, null)));
        assertThrows(IllegalArgumentException.class, () -> caseStudyCreator.createEvaluator(
                new EvaluateRequestDTO(null, "A", "CASE_STUDY", null, List.of())));
        assertThrows(IllegalArgumentException.class, () -> caseStudyCreator.createEvaluator(
                new EvaluateRequestDTO(null, "A", "CASE_STUDY", null, null)));
    }
}
