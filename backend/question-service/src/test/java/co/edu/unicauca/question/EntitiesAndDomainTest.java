package co.edu.unicauca.question;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.question.domain.model.Context;
import co.edu.unicauca.question.domain.model.Option;
import co.edu.unicauca.question.domain.model.Question;
import co.edu.unicauca.question.dto.OptionDTO;
import co.edu.unicauca.question.dto.QuestionFilter;
import co.edu.unicauca.question.dto.QuestionRequestDTO;
import co.edu.unicauca.question.model.OperationEntity;
import co.edu.unicauca.question.model.OptionEmbeddable;
import co.edu.unicauca.question.model.QuestionEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntitiesAndDomainTest {

    @Test
    void questionEntityStoresContentAndReplacesOptionsOnUpdate() {
        QuestionEntity entity = new QuestionEntity();
        entity.setAuthorId(7L);
        entity.updateContent("ctx", "stmt", List.of(new OptionEmbeddable("A", "uno")), "A", "just", "comp", "tema",
                "sub", "bib", "HIGH");
        Instant firstUpdate = entity.getUpdatedAt();

        entity.updateContent("ctx2", "stmt2", List.of(new OptionEmbeddable("B", "dos"),
                new OptionEmbeddable("C", "tres")), "B", "just2", "comp2", "tema2", "sub2", "bib2", "BASIC");

        assertEquals(7L, entity.getAuthorId());
        assertEquals("ctx2", entity.getContext());
        assertEquals("stmt2", entity.getStatement());
        assertEquals(2, entity.getOptions().size());
        assertEquals("B", entity.getCorrectOption());
        assertEquals("just2", entity.getJustification());
        assertEquals("comp2", entity.getCompetency());
        assertEquals("tema2", entity.getTopic());
        assertEquals("sub2", entity.getSubtopic());
        assertEquals("bib2", entity.getBibliography());
        assertEquals("BASIC", entity.getDifficulty());
        assertNotNull(entity.getCreatedAt());
        assertFalse(entity.getUpdatedAt().isBefore(firstUpdate));
        assertNull(entity.getId());
        entity.setExternalCode("EXT-1");
        assertEquals("EXT-1", entity.getExternalCode());
    }

    @Test
    void optionEmbeddableExposesLabelAndText() {
        OptionEmbeddable option = new OptionEmbeddable("A", "texto");

        assertEquals("A", option.getLabel());
        assertEquals("texto", option.getText());
    }

    @Test
    void operationEntityStartsPendingAndFinishesWithTheResult() {
        OperationEntity operation = new OperationEntity("op-1", 5L, "SubmitQuestionForReview");

        assertEquals("PENDING", operation.getStatus());
        assertNull(operation.getCompletedAt());

        Instant at = Instant.now();
        operation.finish("COMPLETED", "ok", at);

        assertEquals("COMPLETED", operation.getStatus());
        assertEquals("ok", operation.getMessage());
        assertEquals(at, operation.getCompletedAt());
        assertEquals("op-1", operation.getOperationId());
        assertEquals(5L, operation.getQuestionId());
        assertEquals("SubmitQuestionForReview", operation.getType());
        assertNotNull(operation.getCreatedAt());
    }

    @Test
    void operationEntityUsesTheCurrentTimeWhenNoCompletionTimeIsGiven() {
        OperationEntity operation = new OperationEntity("op-2", 5L, "T");

        operation.finish("FAILED", "x", null);

        assertNotNull(operation.getCompletedAt());
    }

    @Test
    void domainQuestionExposesItsPartsAndTheCorrectOption() {
        List<Option> options = new ArrayList<>(List.of(new Option("A", " uno ", false), new Option("B", "dos", true)));
        Question question = new Question("Q1", new Context("  contexto "), "enunciado", options, "justificación",
                "competencia", "tema", "subtema");

        assertEquals("Q1", question.getId());
        assertEquals("contexto", question.getContext().getText());
        assertEquals("enunciado", question.getStatement());
        assertEquals("justificación", question.getJustification());
        assertEquals("competencia", question.getCompetency());
        assertEquals("tema", question.getTopic());
        assertEquals("subtema", question.getSubtopic());
        assertEquals("B", question.getCorrectOption().orElseThrow().getLabel());
        assertEquals("uno", question.getOptions().get(0).getText());
        assertThrows(UnsupportedOperationException.class, () -> question.getOptions().add(new Option("C", "x", false)));
        assertTrue(question.toString().contains("Enunciado: enunciado"));
    }

    @Test
    void domainQuestionWithoutCorrectOptionReturnsEmpty() {
        Question question = new Question("Q2", new Context(null), "s", List.of(new Option("A", null, false)),
                "j", "c", "t", "st");

        assertTrue(question.getCorrectOption().isEmpty());
        assertTrue(question.getContext().isBlank());
        assertEquals("", question.getOptions().get(0).getText());
        assertEquals("A) ", question.getOptions().get(0).toString());
    }

    @Test
    void dtoCopiesReplaceTheAuthorOnly() {
        QuestionRequestDTO original = new QuestionRequestDTO(null, "c", "s", List.of(new OptionDTO("A", "x")), "A",
                "j", "k", "t", "st", "b", "MEDIUM");

        QuestionRequestDTO copy = original.withAuthorId(7L);
        QuestionFilter filter = new QuestionFilter(null, "DRAFT", "q", "c", "t", "s", "HIGH").withAuthorId(9L);

        assertEquals(7L, copy.authorId());
        assertEquals("s", copy.statement());
        assertEquals("MEDIUM", copy.difficulty());
        assertEquals(9L, filter.authorId());
        assertEquals("DRAFT", filter.status());
    }
}
