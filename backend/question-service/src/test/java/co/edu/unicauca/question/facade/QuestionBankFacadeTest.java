package co.edu.unicauca.question.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.question.dto.OptionDTO;
import co.edu.unicauca.question.dto.PageDTO;
import co.edu.unicauca.question.dto.QuestionFilter;
import co.edu.unicauca.question.dto.QuestionRequestDTO;
import co.edu.unicauca.question.dto.QuestionResponseDTO;
import co.edu.unicauca.question.dto.SubmitAcceptedDTO;
import co.edu.unicauca.question.dto.SubmitRequestDTO;
import co.edu.unicauca.question.exception.ForbiddenException;
import co.edu.unicauca.question.messaging.SubmissionNotifier;
import co.edu.unicauca.question.repository.QuestionRepository;
import co.edu.unicauca.question.security.AuthenticatedUser;
import co.edu.unicauca.question.security.CurrentUserProvider;
import co.edu.unicauca.question.service.QuestionService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestionBankFacadeTest {

    @Mock
    private QuestionService questionService;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private CurrentUserProvider currentUser;
    @Mock
    private SubmissionNotifier notifier;

    private QuestionBankFacade facade;

    private final AuthenticatedUser author = new AuthenticatedUser(7L, "docente1", Set.of("DOCENTE", "AUTOR"));
    private final AuthenticatedUser admin = new AuthenticatedUser(2L, "admin1", Set.of("ADMIN"));

    @BeforeEach
    void setUp() {
        facade = new QuestionBankFacade(questionService, questionRepository, currentUser, notifier);
    }

    private static QuestionRequestDTO request(Long authorId) {
        return new QuestionRequestDTO(authorId, "c", "s", List.of(new OptionDTO("A", "x")), "A", "j", "k", "t", "st",
                "b", "MEDIUM");
    }

    private static QuestionResponseDTO response(long id, long authorId, String status) {
        return new QuestionResponseDTO(id, authorId, status, "c", "s", List.of(), "A", "j", "k", "t", "st",
                Instant.now(), Instant.now(), "b", "MEDIUM");
    }

    @Test
    void createTakesTheAuthorFromTheToken() {
        when(currentUser.current()).thenReturn(author);
        when(questionService.create(any())).thenReturn(response(1L, 7L, "DRAFT"));

        facade.createQuestion(request(null));

        ArgumentCaptor<QuestionRequestDTO> captor = ArgumentCaptor.forClass(QuestionRequestDTO.class);
        verify(questionService).create(captor.capture());
        assertEquals(7L, captor.getValue().authorId());
    }

    @Test
    void createRejectsAnAuthorIdThatIsNotTheCaller() {
        when(currentUser.current()).thenReturn(author);

        assertThrows(ForbiddenException.class, () -> facade.createQuestion(request(8L)));
        verify(questionService, never()).create(any());
    }

    @Test
    void authorsListOnlyTheirOwnQuestionsWhileAdministratorsListAll() {
        when(currentUser.current()).thenReturn(author, admin);
        when(questionService.listByAuthor(7L)).thenReturn(List.of(response(1L, 7L, "DRAFT")));
        when(questionService.list()).thenReturn(List.of(response(1L, 7L, "DRAFT"), response(2L, 8L, "DRAFT")));

        assertEquals(1, facade.listQuestions().size());
        assertEquals(2, facade.listQuestions().size());
    }

    @Test
    void searchIsScopedToTheAuthorEvenIfAnotherAuthorIsRequested() {
        when(currentUser.current()).thenReturn(author);

        assertThrows(ForbiddenException.class, () -> facade.searchQuestions(
                new QuestionFilter(8L, null, null, null, null, null, null), 0, 10));
    }

    @Test
    void searchForcesTheAuthorFilterForAuthorsAndLeavesItFreeForAdministrators() {
        PageDTO<QuestionResponseDTO> page = new PageDTO<>(List.of(), 0, 10, 0, 0);
        when(currentUser.current()).thenReturn(author, admin);
        when(questionService.search(any(), eq(0), eq(10))).thenReturn(page);

        facade.searchQuestions(new QuestionFilter(null, "DRAFT", null, null, null, null, null), 0, 10);
        facade.searchQuestions(new QuestionFilter(null, "DRAFT", null, null, null, null, null), 0, 10);

        ArgumentCaptor<QuestionFilter> captor = ArgumentCaptor.forClass(QuestionFilter.class);
        verify(questionService, org.mockito.Mockito.times(2)).search(captor.capture(), eq(0), eq(10));
        assertEquals(7L, captor.getAllValues().get(0).authorId());
        assertEquals(null, captor.getAllValues().get(1).authorId());
    }

    @Test
    void anAuthorCannotReadSomeoneElsesQuestionButAnAdministratorCan() {
        when(currentUser.current()).thenReturn(author, admin);
        when(questionService.get(5L)).thenReturn(response(5L, 8L, "DRAFT"));

        assertThrows(ForbiddenException.class, () -> facade.findQuestion(5L));
        assertEquals(5L, facade.findQuestion(5L).id());
    }

    @Test
    void submitNotifiesAdministratorsForADraft() {
        when(currentUser.current()).thenReturn(author);
        when(questionService.get(5L)).thenReturn(response(5L, 7L, "DRAFT"));
        SubmitAcceptedDTO accepted = new SubmitAcceptedDTO("ok", "op", 5L, "PENDING", "/operations/op");
        when(questionService.submitForReview(5L, 7L)).thenReturn(accepted);

        assertEquals(accepted, facade.submitForReview(5L, new SubmitRequestDTO(7L)));

        verify(notifier).notifyAdministrators(author, 5L, false);
    }

    @Test
    void submitAfterARejectionIsAnnouncedAsResubmission() {
        when(currentUser.current()).thenReturn(author);
        when(questionService.get(5L)).thenReturn(response(5L, 7L, "REJECTED"));
        when(questionService.submitForReview(5L, 7L))
                .thenReturn(new SubmitAcceptedDTO("ok", "op", 5L, "PENDING", "/operations/op"));

        facade.submitForReview(5L, null);

        verify(notifier).notifyAdministrators(author, 5L, true);
    }

    @Test
    void submitDoesNotNotifyWhenTheStateCannotBeSubmitted() {
        when(currentUser.current()).thenReturn(author);
        when(questionService.get(5L)).thenReturn(response(5L, 7L, "IN_REVIEW"));
        when(questionService.submitForReview(5L, 7L))
                .thenReturn(new SubmitAcceptedDTO("ok", "op", 5L, "PENDING", "/operations/op"));

        facade.submitForReview(5L, null);

        verify(notifier, never()).notifyAdministrators(any(), any(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void onlyTheAuthorCanSubmitAndNobodyCanSubmitInSomeoneElsesName() {
        when(currentUser.current()).thenReturn(admin, author);
        when(questionService.get(5L)).thenReturn(response(5L, 7L, "DRAFT"));

        assertThrows(ForbiddenException.class, () -> facade.submitForReview(5L, null));
        assertThrows(ForbiddenException.class, () -> facade.submitForReview(5L, new SubmitRequestDTO(99L)));
        verify(questionService, never()).submitForReview(any(), any());
    }

    @Test
    void updateTakesTheAuthorFromTheToken() {
        when(currentUser.current()).thenReturn(author);
        when(questionService.update(eq(5L), any())).thenReturn(response(5L, 7L, "DRAFT"));

        facade.updateQuestion(5L, request(null));

        ArgumentCaptor<QuestionRequestDTO> captor = ArgumentCaptor.forClass(QuestionRequestDTO.class);
        verify(questionService).update(eq(5L), captor.capture());
        assertEquals(7L, captor.getValue().authorId());
    }
}
