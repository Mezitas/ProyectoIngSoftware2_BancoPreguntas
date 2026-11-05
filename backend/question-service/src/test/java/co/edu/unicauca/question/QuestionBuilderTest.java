package co.edu.unicauca.question;

import co.edu.unicauca.question.builder.QuestionBuilder;
import co.edu.unicauca.question.builder.QuestionDirector;
import co.edu.unicauca.question.domain.model.Question;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuestionBuilderTest {
    @Test
    void buildPreguntaValida() {
        Question q = new QuestionBuilder().setStatement("¿2+2?")
                .addOption("3", false).addOption("4", true).addOption("5", false).addOption("6", false)
                .build();
        assertEquals("¿2+2?", q.getStatement());
        assertEquals(4, q.getOptions().size());
        assertEquals("B", q.getCorrectOption().orElseThrow().getLabel());
    }

    @Test
    void generaIdSiNoSeIndica() {
        Question q = new QuestionBuilder().setStatement("S").addOption("a", true)
                .addOption("b", false).addOption("c", false).addOption("d", false).build();
        assertNotNull(q.getId());
        assertFalse(q.getId().isBlank());
    }

    @Test
    void rechazaMenosDe4Opciones() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new QuestionBuilder().setStatement("S").addOption("a", true)
                        .addOption("b", false).addOption("c", false).build());
        assertTrue(ex.getMessage().contains("4 opciones"));
    }

    @Test
    void rechazaMasDe1Correcta() {
        assertThrows(IllegalStateException.class,
                () -> new QuestionBuilder().setStatement("S").addOption("a", true)
                        .addOption("b", true).addOption("c", false).addOption("d", false).build());
    }

    @Test
    void rechazaSinCorrecta() {
        assertThrows(IllegalStateException.class,
                () -> new QuestionBuilder().setStatement("S").addOption("a", false)
                        .addOption("b", false).addOption("c", false).addOption("d", false).build());
    }

    @Test
    void rechazaSinEnunciado() {
        assertThrows(IllegalStateException.class,
                () -> new QuestionBuilder().addOption("a", true).addOption("b", false)
                        .addOption("c", false).addOption("d", false).build());
    }

    @Test
    void builderReusableTrasFallo() {
        QuestionBuilder b = new QuestionBuilder();
        assertThrows(IllegalStateException.class, () -> b.setStatement("S").addOption("a", true).build());
        Question q = b.setStatement("Ok").addOption("a", true).addOption("b", false)
                .addOption("c", false).addOption("d", false).build();
        assertEquals("Ok", q.getStatement());
    }

    @Test
    void directorUbicaCorrectaEnPosicion() {
        QuestionDirector director = new QuestionDirector(new QuestionBuilder());
        Question q = director.buildMultipleChoice("c", "s", "correcta",
                List.of("d1", "d2", "d3"), 2, "j", "comp", "tema", "sub");
        assertEquals("C", q.getCorrectOption().orElseThrow().getLabel());
        assertEquals("correcta", q.getOptions().get(2).getText());
    }

    @Test
    void directorConPosicionExcedidaDejaLaCorrectaDeUltima() {
        QuestionDirector director = new QuestionDirector(new QuestionBuilder());
        Question q = director.buildMultipleChoice("c", "s", "correcta",
                List.of("d1", "d2", "d3"), 99, "j", "comp", "tema", "sub");
        assertEquals("D", q.getCorrectOption().orElseThrow().getLabel());
    }

    @Test
    void preguntaMuestraDelDirectorEsValida() {
        assertEquals(4, new QuestionDirector(new QuestionBuilder()).buildSampleQuestion().getOptions().size());
    }
}
