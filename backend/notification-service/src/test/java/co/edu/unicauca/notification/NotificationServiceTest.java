package co.edu.unicauca.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.notification.messaging.QuestionNotificationEvent;
import co.edu.unicauca.notification.messaging.ReviewCompletedEvent;
import co.edu.unicauca.notification.messaging.ReviewerAssignedEvent;
import co.edu.unicauca.notification.model.NotificationEntity;
import co.edu.unicauca.notification.repository.NotificationRepository;
import co.edu.unicauca.notification.service.EmailService;
import co.edu.unicauca.notification.service.NotificationService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;
    @Mock
    private EmailService emailService;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(repository, emailService);
    }

    private void stubSave() {
        when(repository.save(any(NotificationEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    private static QuestionNotificationEvent questionEvent(String type) {
        return new QuestionNotificationEvent("evt-" + type, type, 12L, 3L, "Carlos Admin", "admin@example.edu",
                "docente1", "aprobó. Observaciones: ok", Instant.now());
    }

    @Test
    void reviewerAssignedSendsAnEmailAndMarksTheNotificationSent() {
        stubSave();
        when(repository.existsByEventId("evt-1")).thenReturn(false);

        service.handleReviewerAssigned(new ReviewerAssignedEvent("evt-1", 12L, 9L, "Rosa", "rosa@example.edu",
                3L, Instant.now()));

        ArgumentCaptor<NotificationEntity> saved = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(repository, times(2)).save(saved.capture());
        assertEquals("SENT", saved.getValue().getStatus());
        assertEquals("REVIEWER_ASSIGNED", saved.getValue().getType());
        verify(emailService).send(eq("rosa@example.edu"), eq("Nueva pregunta asignada para revisión"), any());
    }

    @Test
    void duplicatedEventsAreIgnored() {
        when(repository.existsByEventId("evt-1")).thenReturn(true);

        service.handleReviewerAssigned(new ReviewerAssignedEvent("evt-1", 12L, 9L, "Rosa", "rosa@example.edu",
                3L, Instant.now()));

        verify(repository, never()).save(any());
        verify(emailService, never()).send(any(), any(), any());
    }

    @Test
    void failedEmailMarksTheNotificationFailed() {
        stubSave();
        when(repository.existsByEventId("evt-2")).thenReturn(false);
        doThrow(new IllegalStateException("smtp caído")).when(emailService).send(any(), any(), any());

        service.handleReviewCompleted(new ReviewCompletedEvent("evt-2", 12L, 7L, "Ana", "ana@example.edu",
                "REJECTED", "Falta justificación", Instant.now()));

        ArgumentCaptor<NotificationEntity> saved = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(repository, times(2)).save(saved.capture());
        assertEquals("FAILED", saved.getValue().getStatus());
    }

    @Test
    void reviewCompletedMessageStatesTheDecisionAndTheObservations() {
        stubSave();
        when(repository.existsByEventId("evt-3")).thenReturn(false);

        service.handleReviewCompleted(new ReviewCompletedEvent("evt-3", 12L, 7L, "Ana", "ana@example.edu",
                "REJECTED", "Falta justificación", Instant.now()));

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq("ana@example.edu"), eq("Evaluación de la pregunta #12 finalizada"),
                message.capture());
        assertTrue(message.getValue().contains("rechazada"));
        assertTrue(message.getValue().contains("Falta justificación"));
    }

    @Test
    void submittedQuestionNotifiesTheAdministratorToAssignReviewers() {
        stubSave();
        when(repository.existsByEventId("evt-QUESTION_SUBMITTED")).thenReturn(false);

        service.handleQuestionEvent(questionEvent("QUESTION_SUBMITTED"));

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq("admin@example.edu"), eq("Pregunta #12 pendiente de revisión"),
                message.capture());
        assertTrue(message.getValue().contains("docente1"));
        assertTrue(message.getValue().contains("entre 1 y 3 revisores"));
    }

    @Test
    void resubmittedQuestionIsAnnouncedAsCorrected() {
        stubSave();
        when(repository.existsByEventId("evt-QUESTION_RESUBMITTED")).thenReturn(false);

        service.handleQuestionEvent(questionEvent("QUESTION_RESUBMITTED"));

        verify(emailService).send(eq("admin@example.edu"), eq("Pregunta #12 reenviada tras correcciones"), any());
    }

    @Test
    void individualReviewNotifiesWithTheReviewerDetail() {
        stubSave();
        when(repository.existsByEventId("evt-REVIEW_SUBMITTED")).thenReturn(false);

        service.handleQuestionEvent(questionEvent("REVIEW_SUBMITTED"));

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq("admin@example.edu"), eq("Nueva evaluación de la pregunta #12"),
                message.capture());
        assertTrue(message.getValue().contains("aprobó. Observaciones: ok"));
    }

    @Test
    void unknownEventTypesAreIgnored() {
        service.handleQuestionEvent(questionEvent("OTHER"));

        verify(repository, never()).save(any());
        verify(emailService, never()).send(any(), any(), any());
    }

    @Test
    void administratorsSeeEveryNotification() {
        NotificationEntity n = new NotificationEntity("e", 7L, "ana@example.edu", "T", "S", "M");
        when(repository.findByRecipientIdOrderByIdAsc(7L)).thenReturn(List.of(n));
        when(repository.findAll()).thenReturn(List.of(n, n));

        assertEquals(1, service.findVisible(7L, "admin@example.edu", true).size());
        assertEquals(2, service.findVisible(null, "admin@example.edu", true).size());
    }

    @Test
    void regularUsersOnlySeeNotificationsSentToTheirOwnEmail() {
        NotificationEntity own = new NotificationEntity("e1", 7L, "ana@example.edu", "T", "S", "M");
        when(repository.findByRecipientEmailIgnoreCaseOrderByIdAsc("ana@example.edu")).thenReturn(List.of(own));

        assertEquals(List.of(own), service.findVisible(null, "ana@example.edu", false));
        assertEquals(List.of(own), service.findVisible(7L, "ana@example.edu", false));
        assertTrue(service.findVisible(8L, "ana@example.edu", false).isEmpty());
        assertTrue(service.findVisible(7L, null, false).isEmpty());
    }
}
