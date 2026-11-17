package co.edu.unicauca.question;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.question.client.LifecycleClient;
import co.edu.unicauca.question.client.UserClient;
import co.edu.unicauca.question.client.ValidationClient;
import co.edu.unicauca.question.dto.LifecycleStateDTO;
import co.edu.unicauca.question.dto.OptionDTO;
import co.edu.unicauca.question.dto.QuestionFilter;
import co.edu.unicauca.question.dto.QuestionRequestDTO;
import co.edu.unicauca.question.dto.UserInfoDTO;
import co.edu.unicauca.question.dto.ValidationResultDTO;
import co.edu.unicauca.question.exception.BadRequestException;
import co.edu.unicauca.question.exception.ConflictException;
import co.edu.unicauca.question.exception.ForbiddenException;
import co.edu.unicauca.question.exception.NotFoundException;
import co.edu.unicauca.question.exception.ServiceUnavailableException;
import co.edu.unicauca.question.exception.ValidationFailedException;
import co.edu.unicauca.question.messaging.OperationResultMessage;
import co.edu.unicauca.question.model.OperationEntity;
import co.edu.unicauca.question.messaging.CommandPublisher;
import co.edu.unicauca.question.model.OptionEmbeddable;
import co.edu.unicauca.question.model.QuestionEntity;
import co.edu.unicauca.question.repository.OperationRepository;
import co.edu.unicauca.question.repository.QuestionRepository;
import co.edu.unicauca.question.service.QuestionService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.amqp.AmqpException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    // ------------------------------------------------------------ helpers
    private static QuestionEntity entity(long id, long authorId) {
        QuestionEntity question = new QuestionEntity();
        ReflectionTestUtils.setField(question, "id", id);
        question.setAuthorId(authorId);
        question.updateContent("context", "statement",
                List.of(new OptionEmbeddable("A", "one"), new OptionEmbeddable("B", "two"),
                        new OptionEmbeddable("C", "three"), new OptionEmbeddable("D", "four")),
                "A", "justification", "competency", "topic", "subtopic", "Source", "MEDIUM");
        return question;
    }

    private static QuestionRequestDTO request(Long authorId) {
        return new QuestionRequestDTO(authorId, "contexto", "enunciado",
                List.of(new OptionDTO("a", " uno "), new OptionDTO("b", "dos"),
                        new OptionDTO("c", "tres"), new OptionDTO("d", "cuatro")),
                "a", "justificación", "competencia", "tema", "subtema", "bibliografía", "media");
    }

    private void authorExists(long id) {
        when(userClient.findById(id)).thenReturn(Optional.of(
                new UserInfoDTO(id, "docente", "Docente", "d@example.edu", Set.of("DOCENTE"))));
    }

    // ------------------------------------------------------------ crear
    @Test
    void createPersistsNormalizedContentAndRegistersTheQuestionAsDraft() {
        authorExists(7L);
        when(validationClient.validate(any())).thenReturn(new ValidationResultDTO(true, List.of()));
        when(questions.save(any(QuestionEntity.class))).thenAnswer(invocation -> {
            QuestionEntity saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 40L);
            return saved;
        });
        when(lifecycleClient.register(40L, 7L)).thenReturn(new LifecycleStateDTO(40L, "DRAFT"));

        var created = service.create(request(7L));

        assertEquals(40L, created.id());
        assertEquals("DRAFT", created.status());
        assertEquals("A", created.correctOption());
        assertEquals("MEDIUM", created.difficulty());
        assertEquals("A", created.options().get(0).label());
        assertEquals("uno", created.options().get(0).text());
    }

    @Test
    void createRejectsUsersWithoutAnAuthorRole() {
        when(userClient.findById(9L)).thenReturn(Optional.of(
                new UserInfoDTO(9L, "estudiante", "Estudiante", "e@example.edu", Set.of("ESTUDIANTE"))));

        assertThrows(ForbiddenException.class, () -> service.create(request(9L)));
        verify(questions, never()).save(any());
    }

    @Test
    void createRejectsUnknownAuthors() {
        when(userClient.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> service.create(request(99L)));
    }

    @Test
    void createFailsWhenTheStructuralValidationFails() {
        authorExists(7L);
        when(validationClient.validate(any()))
                .thenReturn(new ValidationResultDTO(false, List.of("La bibliografía es obligatoria")));

        var failure = assertThrows(ValidationFailedException.class, () -> service.create(request(7L)));

        assertEquals(List.of("La bibliografía es obligatoria"), failure.getErrors());
        verify(questions, never()).save(any());
    }

    @Test
    void createCompensatesWhenLifecycleCannotRegisterTheQuestion() {
        authorExists(7L);
        when(validationClient.validate(any())).thenReturn(new ValidationResultDTO(true, List.of()));
        when(questions.save(any(QuestionEntity.class))).thenAnswer(invocation -> {
            QuestionEntity saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 41L);
            return saved;
        });
        when(lifecycleClient.register(41L, 7L)).thenThrow(new ServiceUnavailableException("caído"));

        assertThrows(ServiceUnavailableException.class, () -> service.create(request(7L)));

        verify(questions).deleteById(41L);
    }

    // ------------------------------------------------------------ consultar
    @Test
    void getFailsWhenTheQuestionDoesNotExist() {
        when(questions.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.get(1L));
    }

    @Test
    void getReportsUnavailableWhenLifecycleIsDown() {
        QuestionEntity question = entity(5L, 7L);
        when(questions.findById(5L)).thenReturn(Optional.of(question));
        when(lifecycleClient.getState(5L)).thenThrow(new ServiceUnavailableException("caído"));

        assertEquals("UNAVAILABLE", service.get(5L).status());
    }

    @Test
    void listByAuthorOnlyReturnsThatAuthorsQuestions() {
        QuestionEntity question = entity(5L, 7L);
        when(questions.findByAuthorIdOrderByIdDesc(7L)).thenReturn(List.of(question));
        when(lifecycleClient.getState(5L)).thenReturn(Optional.of(new LifecycleStateDTO(5L, "DRAFT")));

        var result = service.listByAuthor(7L);

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).authorId());
    }

    // ------------------------------------------------------------ búsqueda paginada
    @SuppressWarnings("unchecked")
    @Test
    void searchPaginatesInTheDatabaseAndResolvesStatusOnlyForThePage() {
        QuestionEntity question = entity(5L, 7L);
        Page<QuestionEntity> page = new PageImpl<>(List.of(question),
                org.springframework.data.domain.PageRequest.of(1, 5), 11);
        when(questions.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(lifecycleClient.getState(5L)).thenReturn(Optional.of(new LifecycleStateDTO(5L, "APPROVED")));

        var result = service.search(new QuestionFilter(7L, null, "texto", null, null, null, "media"), 1, 5);

        assertEquals(1, result.page());
        assertEquals(5, result.size());
        assertEquals(11, result.totalElements());
        assertEquals(3, result.totalPages());
        assertEquals("APPROVED", result.content().get(0).status());
        verify(questions, never()).findAll(any(Specification.class), any(Sort.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchClampsPageAndSizeToValidBounds() {
        when(questions.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var result = service.search(new QuestionFilter(null, null, null, null, null, null, null), -3, 100000);

        assertEquals(0, result.page());
        assertEquals(100, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchWithStatusFilterPaginatesTheMatchingQuestions() {
        QuestionEntity first = entity(1L, 7L);
        QuestionEntity second = entity(2L, 7L);
        QuestionEntity third = entity(3L, 7L);
        when(questions.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(first, second, third));
        when(lifecycleClient.getState(1L)).thenReturn(Optional.of(new LifecycleStateDTO(1L, "DRAFT")));
        when(lifecycleClient.getState(2L)).thenReturn(Optional.of(new LifecycleStateDTO(2L, "REJECTED")));
        when(lifecycleClient.getState(3L)).thenReturn(Optional.of(new LifecycleStateDTO(3L, "DRAFT")));

        var firstPage = service.search(new QuestionFilter(7L, "draft", null, null, null, null, null), 0, 1);
        var secondPage = service.search(new QuestionFilter(7L, "draft", null, null, null, null, null), 1, 1);
        var beyond = service.search(new QuestionFilter(7L, "draft", null, null, null, null, null), 5, 1);

        assertEquals(2, firstPage.totalElements());
        assertEquals(2, firstPage.totalPages());
        assertEquals(1L, firstPage.content().get(0).id());
        assertEquals(3L, secondPage.content().get(0).id());
        assertEquals(0, beyond.content().size());
    }

    @Test
    void searchRejectsUnknownStatuses() {
        assertThrows(BadRequestException.class,
                () -> service.search(new QuestionFilter(null, "NOPE", null, null, null, null, null), 0, 10));
    }

    // ------------------------------------------------------------ editar
    @Test
    void updateIsOnlyAllowedForTheAuthor() {
        when(questions.findById(5L)).thenReturn(Optional.of(entity(5L, 7L)));

        assertThrows(ForbiddenException.class, () -> service.update(5L, request(8L)));
    }

    @Test
    void updateIsRejectedOnceTheQuestionLeftDraft() {
        when(questions.findById(5L)).thenReturn(Optional.of(entity(5L, 7L)));
        when(lifecycleClient.getState(5L)).thenReturn(Optional.of(new LifecycleStateDTO(5L, "IN_REVIEW")));

        assertThrows(ConflictException.class, () -> service.update(5L, request(7L)));
    }

    @Test
    void updateIsRefusedWhenLifecycleIsUnavailable() {
        when(questions.findById(5L)).thenReturn(Optional.of(entity(5L, 7L)));
        when(lifecycleClient.getState(5L)).thenThrow(new ServiceUnavailableException("caído"));

        assertThrows(ServiceUnavailableException.class, () -> service.update(5L, request(7L)));
    }

    @Test
    void updatingARejectedQuestionReturnsItToDraft() {
        QuestionEntity question = entity(5L, 7L);
        when(questions.findById(5L)).thenReturn(Optional.of(question));
        when(lifecycleClient.getState(5L)).thenReturn(Optional.of(new LifecycleStateDTO(5L, "REJECTED")));
        when(validationClient.validate(any())).thenReturn(new ValidationResultDTO(true, List.of()));
        when(questions.save(any(QuestionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = service.update(5L, request(7L));

        verify(lifecycleClient).transition(5L, "DRAFT", 7L, "Pregunta corregida por el autor");
        assertEquals("DRAFT", updated.status());
        assertEquals("enunciado", updated.statement());
    }

    // ------------------------------------------------------------ envío a revisión
    @Test
    void submitPublishesTheCommandAndRegistersAPendingOperation() {
        when(questions.findById(5L)).thenReturn(Optional.of(entity(5L, 7L)));

        var accepted = service.submitForReview(5L, null);

        assertEquals("PENDING", accepted.status());
        assertEquals(5L, accepted.questionId());
        verify(operations).save(any(OperationEntity.class));
        verify(publisher).publishSubmit(any());
    }

    @Test
    void submitFailsCleanlyWhenTheBrokerIsDown() {
        when(questions.findById(5L)).thenReturn(Optional.of(entity(5L, 7L)));
        doThrow(new AmqpException("rabbit caído")).when(publisher).publishSubmit(any());
        when(operations.findById(anyString())).thenAnswer(invocation ->
                Optional.of(new OperationEntity(invocation.getArgument(0), 5L, "SubmitQuestionForReview")));

        assertThrows(ServiceUnavailableException.class, () -> service.submitForReview(5L, 7L));
    }

    // ------------------------------------------------------------ operaciones
    @Test
    void applyOperationResultFinishesTheOperation() {
        OperationEntity operation = new OperationEntity("op-1", 5L, "SubmitQuestionForReview");
        when(operations.findById("op-1")).thenReturn(Optional.of(operation));

        service.applyOperationResult(new OperationResultMessage("op-1", 5L, "SubmitQuestionForReview",
                "COMPLETED", "ok", Instant.now()));

        assertEquals("COMPLETED", operation.getStatus());
        verify(operations).save(operation);
    }

    @Test
    void applyOperationResultIgnoresUnknownOperations() {
        when(operations.findById("ghost")).thenReturn(Optional.empty());

        service.applyOperationResult(new OperationResultMessage("ghost", 5L, "SubmitQuestionForReview",
                "COMPLETED", "ok", Instant.now()));

        verify(operations, never()).save(any());
    }

    @Test
    void getOperationReportsItsStateOrFailsWhenUnknown() {
        when(operations.findById("op-2")).thenReturn(Optional.of(new OperationEntity("op-2", 5L, "T")));
        when(operations.findById("ghost")).thenReturn(Optional.empty());

        assertEquals("PENDING", service.getOperation("op-2").status());
        assertThrows(NotFoundException.class, () -> service.getOperation("ghost"));
    }
}
