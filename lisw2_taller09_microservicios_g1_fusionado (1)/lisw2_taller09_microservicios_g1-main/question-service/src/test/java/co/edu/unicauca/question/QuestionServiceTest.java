package co.edu.unicauca.question;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.question.client.LifecycleClient;
import co.edu.unicauca.question.client.UserClient;
import co.edu.unicauca.question.client.ValidationClient;
import co.edu.unicauca.question.dto.LifecycleStateDTO;
import co.edu.unicauca.question.messaging.CommandPublisher;
import co.edu.unicauca.question.model.OptionEmbeddable;
import co.edu.unicauca.question.model.QuestionEntity;
import co.edu.unicauca.question.repository.OperationRepository;
import co.edu.unicauca.question.repository.QuestionRepository;
import co.edu.unicauca.question.service.QuestionService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questions;
    @Mock
    private OperationRepository operations;
    @Mock
    private ValidationClient validationClient;
    @Mock
    private UserClient userClient;
    @Mock
    private LifecycleClient lifecycleClient;
    @Mock
    private CommandPublisher publisher;
    @InjectMocks
    private QuestionService service;

    @Test
    void listReturnsLifecycleStatusAndPersistedBibliographyAndDifficulty() {
        QuestionEntity question = new QuestionEntity();
        ReflectionTestUtils.setField(question, "id", 24L);
        question.setAuthorId(7L);
        question.updateContent("context", "statement",
                List.of(new OptionEmbeddable("A", "one"), new OptionEmbeddable("B", "two"),
                        new OptionEmbeddable("C", "three"), new OptionEmbeddable("D", "four")),
                "A", "justification", "competency", "topic", "subtopic",
                "Source reference", "HIGH");
        when(questions.findAll()).thenReturn(List.of(question));
        when(lifecycleClient.getState(24L))
                .thenReturn(Optional.of(new LifecycleStateDTO(24L, "PENDING_REVIEW")));

        var result = service.list().get(0);

        assertEquals("PENDING_REVIEW", result.status());
        assertEquals("Source reference", result.bibliography());
        assertEquals("HIGH", result.difficulty());
        verify(lifecycleClient).getState(24L);
    }

    @Test
    void listRegistersLegacyQuestionMissingLifecycleStateAsDraft() {
        QuestionEntity question = new QuestionEntity();
        ReflectionTestUtils.setField(question, "id", 25L);
        question.setAuthorId(8L);
        question.updateContent("context", "statement", List.of(), "A", "justification",
                "competency", "topic", "subtopic", "Source reference", "MEDIUM");
        when(questions.findAll()).thenReturn(List.of(question));
        when(lifecycleClient.getState(25L)).thenReturn(Optional.empty());
        when(lifecycleClient.register(25L, 8L))
                .thenReturn(new LifecycleStateDTO(25L, "DRAFT"));

        var result = service.list().get(0);

        assertEquals("DRAFT", result.status());
        verify(lifecycleClient).register(25L, 8L);
    }
}
