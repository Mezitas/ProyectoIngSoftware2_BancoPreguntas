package co.edu.unicauca.question.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.question.client.UserClient;
import co.edu.unicauca.question.dto.UserInfoDTO;
import co.edu.unicauca.question.security.AuthenticatedUser;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubmissionNotifierTest {

    @Mock
    private UserClient userClient;
    @Mock
    private QuestionEventPublisher publisher;
    @InjectMocks
    private SubmissionNotifier notifier;

    private final AuthenticatedUser author = new AuthenticatedUser(7L, "docente1", Set.of("DOCENTE"));

    @Test
    void publishesOneEventPerAdministrator() {
        when(userClient.findByRole("ADMIN")).thenReturn(List.of(
                new UserInfoDTO(2L, "admin1", "Carlos", "carlos@example.edu", Set.of("ADMIN")),
                new UserInfoDTO(3L, "admin2", "Lucía", "lucia@example.edu", Set.of("ADMIN"))));

        notifier.notifyAdministrators(author, 5L, false);

        ArgumentCaptor<QuestionNotificationEvent> captor = ArgumentCaptor.forClass(QuestionNotificationEvent.class);
        verify(publisher, times(2)).publish(captor.capture());
        QuestionNotificationEvent first = captor.getAllValues().get(0);
        assertEquals("QUESTION_SUBMITTED", first.type());
        assertEquals(5L, first.questionId());
        assertEquals("carlos@example.edu", first.recipientEmail());
        assertEquals("docente1", first.actorName());
    }

    @Test
    void resubmissionsUseTheirOwnEventType() {
        when(userClient.findByRole("ADMIN")).thenReturn(List.of(
                new UserInfoDTO(2L, "admin1", "Carlos", "carlos@example.edu", Set.of("ADMIN"))));

        notifier.notifyAdministrators(author, 5L, true);

        ArgumentCaptor<QuestionNotificationEvent> captor = ArgumentCaptor.forClass(QuestionNotificationEvent.class);
        verify(publisher).publish(captor.capture());
        assertEquals("QUESTION_RESUBMITTED", captor.getValue().type());
    }

    @Test
    void doesNothingWhenThereAreNoAdministrators() {
        when(userClient.findByRole("ADMIN")).thenReturn(List.of());

        notifier.notifyAdministrators(author, 5L, false);

        verify(publisher, never()).publish(any());
    }

    @Test
    void aNotificationFailureNeverBreaksTheSubmission() {
        when(userClient.findByRole("ADMIN")).thenReturn(List.of(
                new UserInfoDTO(2L, "admin1", "Carlos", "carlos@example.edu", Set.of("ADMIN"))));
        doThrow(new IllegalStateException("rabbit caído")).when(publisher).publish(any());

        notifier.notifyAdministrators(author, 5L, false);

        verify(publisher).publish(any());
    }
}
