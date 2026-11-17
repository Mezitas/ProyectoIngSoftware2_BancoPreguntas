package co.edu.unicauca.notification;

import static org.mockito.Mockito.verify;

import co.edu.unicauca.notification.messaging.NotificationListener;
import co.edu.unicauca.notification.messaging.QuestionNotificationEvent;
import co.edu.unicauca.notification.messaging.ReviewCompletedEvent;
import co.edu.unicauca.notification.messaging.ReviewerAssignedEvent;
import co.edu.unicauca.notification.service.NotificationService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private NotificationService service;
    @InjectMocks
    private NotificationListener listener;

    @Test
    void delegatesEveryEventTypeToTheService() {
        ReviewerAssignedEvent assigned = new ReviewerAssignedEvent("a", 1L, 2L, "R", "r@example.edu", 3L,
                Instant.now());
        ReviewCompletedEvent completed = new ReviewCompletedEvent("b", 1L, 2L, "R", "r@example.edu", "APPROVED",
                null, Instant.now());
        QuestionNotificationEvent question = new QuestionNotificationEvent("c", "QUESTION_SUBMITTED", 1L, 2L, "R",
                "r@example.edu", "docente1", null, Instant.now());

        listener.onReviewerAssigned(assigned);
        listener.onReviewCompleted(completed);
        listener.onQuestionEvent(question);

        verify(service).handleReviewerAssigned(assigned);
        verify(service).handleReviewCompleted(completed);
        verify(service).handleQuestionEvent(question);
    }
}
