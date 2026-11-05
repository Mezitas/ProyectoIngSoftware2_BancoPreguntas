package co.edu.unicauca.question;

import co.edu.unicauca.question.adapter.ExternalQuestionAdapter;
import co.edu.unicauca.question.adapter.ExternalQuestionService;
import co.edu.unicauca.question.domain.model.Question;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExternalQuestionAdapterTest {
    private final ExternalQuestionAdapter adapter =
            new ExternalQuestionAdapter(new ExternalQuestionService());

    @Test
    void importaJsonYCrudosOmintiendoInvalidos() {
        List<Question> questions = adapter.getQuestions();
        assertEquals(4, questions.size());
        assertTrue(questions.stream().anyMatch(q -> q.getId().equals("EXT-101")));
        assertTrue(questions.stream().anyMatch(q -> q.getId().equals("EXT-202")));
    }

    @Test
    void traduceCamposDelJson() {
        Question q = adapter.getQuestions().stream()
                .filter(x -> x.getId().equals("EXT-101")).findFirst().orElseThrow();
        assertEquals("¿Qué principio SOLID se cumple al poder extender el sistema sin modificarlo?", q.getStatement());
        assertEquals("A", q.getCorrectOption().orElseThrow().getLabel());
        assertEquals("Abierto/Cerrado", q.getCorrectOption().orElseThrow().getText());
        assertEquals("Principios SOLID", q.getTopic());
    }

    @Test
    void traduceFormatoCrudo() {
        Question q = adapter.getQuestions().stream()
                .filter(x -> x.getId().equals("EXT-201")).findFirst().orElseThrow();
        assertEquals("B", q.getCorrectOption().orElseThrow().getLabel());
    }

    @Test
    void reportaReactivosOmitidos() {
        adapter.getQuestions();
        assertTrue(adapter.getWarnings().stream()
                .anyMatch(w -> w.contains("4 opciones") || w.contains("exactamente")));
    }

    @Test
    void advertenciasSeLimpiarEntreLlamadas() {
        adapter.getQuestions();
        int first = adapter.getWarnings().size();
        adapter.getQuestions();
        assertEquals(first, adapter.getWarnings().size());
    }
}
