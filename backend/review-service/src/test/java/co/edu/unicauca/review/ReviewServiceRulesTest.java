package co.edu.unicauca.review;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.review.client.LifecycleClient;
import co.edu.unicauca.review.client.UserClient;
import co.edu.unicauca.review.dto.AssignRequestDTO;
import co.edu.unicauca.review.dto.LifecycleStateDTO;
import co.edu.unicauca.review.dto.ReviewResultRequestDTO;
import co.edu.unicauca.review.dto.UserInfoDTO;
import co.edu.unicauca.review.exception.BadRequestException;
import co.edu.unicauca.review.exception.ConflictException;
import co.edu.unicauca.review.exception.ForbiddenException;
import co.edu.unicauca.review.exception.NotFoundException;
import co.edu.unicauca.review.exception.UnprocessableException;
import co.edu.unicauca.review.messaging.EventPublisher;
import co.edu.unicauca.review.messaging.QuestionNotificationEvent;
import co.edu.unicauca.review.model.AssignmentStatus;
import co.edu.unicauca.review.model.ReviewAssignmentEntity;
import co.edu.unicauca.review.model.ReviewEntity;
import co.edu.unicauca.review.model.ReviewResult;
import co.edu.unicauca.review.repository.ReviewAssignmentRepository;
import co.edu.unicauca.review.repository.ReviewRepository;
import co.edu.unicauca.review.security.AuthenticatedUser;
import co.edu.unicauca.review.service.ReviewService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewServiceRulesTest {

    @Mock
    private ReviewAssignmentRepository assignments;
    @Mock
    private ReviewRepository reviews;
    @Mock
    private UserClient userClient;
    @Mock
    private LifecycleClient lifecycleClient;
    @Mock
    private EventPublisher publisher;

    private ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewService(assignments, reviews, userClient, lifecycleClient, publisher);
    }

    private static UserInfoDTO user(long id, String username, String role) {
        return new UserInfoDTO(id, username, username + " Nombre", username + "@example.edu", Set.of(role));
    }

    private void admin() {
        when(userClient.findById(100L)).thenReturn(Optional.of(user(100L, "admin", "ADMIN")));
    }

    private void reviewer(long id) {
        when(userClient.findById(id)).thenReturn(Optional.of(user(id, "revisor" + id, "REVISOR")));
    }

    private void state(String status) {
        when(lifecycleClient.getState(55L)).thenReturn(Optional.of(new LifecycleStateDTO(55L, status, 7L)));
    }

    private static ReviewAssignmentEntity confirmed(long reviewerId) {
        ReviewAssignmentEntity assignment = new ReviewAssignmentEntity(55L, reviewerId, 100L);
        ReflectionTestUtils.setField(assignment, "id", reviewerId);
        assignment.changeStatus(AssignmentStatus.CONFIRMED, null);
        return assignment;
    }

    private void activeAssignment(ReviewAssignmentEntity assignment) {
        when(assignments.findFirstByQuestionIdAndReviewerIdAndStatus(55L, assignment.getReviewerId(),
                AssignmentStatus.CONFIRMED)).thenReturn(Optional.of(assignment));
        when(reviews.existsByAssignmentId(assignment.getId())).thenReturn(false);
        when(reviews.save(any(ReviewEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    // ------------------------------------------------------------ asignar
    @Test
    void onlyAdministratorsCanAssignReviewers() {
        when(userClient.findById(100L)).thenReturn(Optional.of(user(100L, "docente", "DOCENTE")));

        assertThrows(ForbiddenException.class, () -> service.assign(new AssignRequestDTO(55L, 201L, 100L)));
    }

    @Test
    void assignFailsForUnknownCallers() {
        when(userClient.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> service.assign(new AssignRequestDTO(55L, 201L, 100L)));
    }

    @Test
    void theAssigneeMustExistAndHaveTheReviewerRole() {
        admin();
        when(userClient.findById(201L)).thenReturn(Optional.empty());
        when(userClient.findById(202L)).thenReturn(Optional.of(user(202L, "docente", "DOCENTE")));

        assertThrows(UnprocessableException.class, () -> service.assign(new AssignRequestDTO(55L, 201L, 100L)));
        assertThrows(UnprocessableException.class, () -> service.assign(new AssignRequestDTO(55L, 202L, 100L)));
    }

    @Test
    void assignRequiresAnExistingQuestionInAnEligibleState() {
        admin();
        reviewer(201L);
        when(lifecycleClient.getState(55L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.assign(new AssignRequestDTO(55L, 201L, 100L)));
    }

    @Test
    void questionsOutsideTheReviewStatesCannotBeAssigned() {
        admin();
        reviewer(201L);
        state("DRAFT");

        assertThrows(ConflictException.class, () -> service.assign(new AssignRequestDTO(55L, 201L, 100L)));
    }

    @Test
    void theAuthorCannotReviewTheirOwnQuestion() {
        admin();
        reviewer(7L);
        state("PENDING_REVIEW");

        assertThrows(UnprocessableException.class, () -> service.assign(new AssignRequestDTO(55L, 7L, 100L)));
    }

    @Test
    void aReviewerCannotBeAssignedTwiceInTheSameRound() {
        admin();
        reviewer(201L);
        state("IN_REVIEW");
        when(assignments.existsByQuestionIdAndReviewerIdAndStatusIn(any(), any(), anyCollection())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.assign(new AssignRequestDTO(55L, 201L, 100L)));
    }

    @Test
    void aQuestionHasAtMostThreeReviewers() {
        admin();
        reviewer(204L);
        state("IN_REVIEW");
        when(assignments.countByQuestionIdAndStatusIn(any(), anyCollection())).thenReturn(3L);

        assertThrows(ConflictException.class, () -> service.assign(new AssignRequestDTO(55L, 204L, 100L)));
        verify(assignments, never()).save(any());
    }

    @Test
    void aFailedTransitionCompensatesTheAssignment() {
        admin();
        reviewer(201L);
        state("PENDING_REVIEW");
        when(assignments.save(any())).thenAnswer(i -> i.getArgument(0));
        doThrow(new ConflictException("rechazada")).when(lifecycleClient)
                .transition(55L, "IN_REVIEW", 100L, "Revisor asignado: revisor201");

        assertThrows(ConflictException.class, () -> service.assign(new AssignRequestDTO(55L, 201L, 100L)));

        ArgumentCaptor<ReviewAssignmentEntity> saved = ArgumentCaptor.forClass(ReviewAssignmentEntity.class);
        verify(assignments, times(2)).save(saved.capture());
        assertEquals(AssignmentStatus.FAILED, saved.getValue().getStatus());
        verify(publisher, never()).publishReviewerAssigned(any());
    }

    @Test
    void aBrokenBrokerDoesNotUndoAValidAssignment() {
        admin();
        reviewer(201L);
        state("IN_REVIEW");
        when(assignments.save(any())).thenAnswer(i -> i.getArgument(0));
        doThrow(new IllegalStateException("rabbit caído")).when(publisher).publishReviewerAssigned(any());

        var result = service.assign(new AssignRequestDTO(55L, 201L, 100L));

        assertEquals(AssignmentStatus.CONFIRMED, result.status());
        assertEquals("NOT_QUEUED", result.notification());
    }

    // ------------------------------------------------------------ evaluar
    @Test
    void rejectingRequiresAReason() {
        assertThrows(BadRequestException.class, () -> service.submitResult(
                new ReviewResultRequestDTO(55L, 201L, ReviewResult.REJECTED, "  ")));
    }

    @Test
    void onlyReviewersWithAnActiveAssignmentCanEvaluate() {
        when(assignments.findFirstByQuestionIdAndReviewerIdAndStatus(55L, 201L, AssignmentStatus.CONFIRMED))
                .thenReturn(Optional.empty());

        assertThrows(ConflictException.class, () -> service.submitResult(
                new ReviewResultRequestDTO(55L, 201L, ReviewResult.APPROVED, "ok")));
    }

    @Test
    void aReviewerCannotEvaluateTwice() {
        ReviewAssignmentEntity assignment = confirmed(201L);
        when(assignments.findFirstByQuestionIdAndReviewerIdAndStatus(55L, 201L, AssignmentStatus.CONFIRMED))
                .thenReturn(Optional.of(assignment));
        when(reviews.existsByAssignmentId(201L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.submitResult(
                new ReviewResultRequestDTO(55L, 201L, ReviewResult.APPROVED, "ok")));
    }

    @Test
    void theLastReviewerRejectingRejectsTheQuestionAndClosesTheRound() {
        ReviewAssignmentEntity assignment = confirmed(201L);
        activeAssignment(assignment);
        when(assignments.countByQuestionIdAndStatus(55L, AssignmentStatus.CONFIRMED)).thenReturn(1L);
        ReviewEntity roundReview = new ReviewEntity(201L, 55L, 201L, ReviewResult.REJECTED, "Falta contexto");
        when(reviews.findByQuestionIdAndClosedFalse(55L)).thenReturn(List.of(roundReview));
        state("REJECTED");

        var result = service.submitResult(new ReviewResultRequestDTO(
                55L, 201L, ReviewResult.REJECTED, "Falta contexto"));

        assertEquals("REJECTED", result.questionStatus());
        verify(lifecycleClient).transition(55L, "REJECTED", 201L, "Falta contexto");
        assertTrue(roundReview.isClosed());
        verify(reviews).saveAll(List.of(roundReview));
    }

    @Test
    void anEarlierRejectionInTheCurrentRoundStillRejectsWhenTheLastReviewerApproves() {
        ReviewAssignmentEntity assignment = confirmed(202L);
        activeAssignment(assignment);
        when(assignments.countByQuestionIdAndStatus(55L, AssignmentStatus.CONFIRMED)).thenReturn(1L);
        when(reviews.existsByQuestionIdAndResultAndClosedFalse(55L, ReviewResult.REJECTED)).thenReturn(true);
        when(reviews.findFirstByQuestionIdAndResultAndClosedFalseOrderByIdAsc(55L, ReviewResult.REJECTED))
                .thenReturn(Optional.of(new ReviewEntity(201L, 55L, 201L, ReviewResult.REJECTED, "Distractor débil")));
        state("REJECTED");

        var result = service.submitResult(new ReviewResultRequestDTO(55L, 202L, ReviewResult.APPROVED, "bien"));

        assertEquals("REJECTED", result.questionStatus());
        verify(lifecycleClient).transition(55L, "REJECTED", 202L, "Distractor débil");
    }

    @Test
    void aRejectionFromAClosedPreviousRoundDoesNotRejectTheCorrectedQuestion() {
        ReviewAssignmentEntity assignment = confirmed(201L);
        activeAssignment(assignment);
        when(assignments.countByQuestionIdAndStatus(55L, AssignmentStatus.CONFIRMED)).thenReturn(1L);
        when(reviews.existsByQuestionIdAndResultAndClosedFalse(55L, ReviewResult.REJECTED)).thenReturn(false);
        state("APPROVED");

        var result = service.submitResult(new ReviewResultRequestDTO(55L, 201L, ReviewResult.APPROVED, "ya corregida"));

        assertEquals("APPROVED", result.questionStatus());
        verify(lifecycleClient).transition(55L, "APPROVED", 201L, "ya corregida");
        verify(reviews, never()).existsByQuestionIdAndResult(any(), any());
    }

    @Test
    void anIntermediateEvaluationNotifiesTheAuthorAndTheAdministrator() {
        ReviewAssignmentEntity assignment = confirmed(201L);
        activeAssignment(assignment);
        when(assignments.countByQuestionIdAndStatus(55L, AssignmentStatus.CONFIRMED)).thenReturn(2L);
        state("IN_REVIEW");
        when(userClient.findById(201L)).thenReturn(Optional.of(user(201L, "revisor201", "REVISOR")));
        when(userClient.findById(7L)).thenReturn(Optional.of(user(7L, "autor", "AUTOR")));
        admin();

        service.submitResult(new ReviewResultRequestDTO(55L, 201L, ReviewResult.APPROVED, "Buen trabajo"));

        ArgumentCaptor<QuestionNotificationEvent> captor = ArgumentCaptor.forClass(QuestionNotificationEvent.class);
        verify(publisher, times(2)).publishQuestionEvent(captor.capture());
        QuestionNotificationEvent toAuthor = captor.getAllValues().get(0);
        assertEquals("REVIEW_SUBMITTED", toAuthor.type());
        assertEquals(7L, toAuthor.recipientId());
        assertEquals("revisor201 Nombre", toAuthor.actorName());
        assertEquals("aprobó. Observaciones: Buen trabajo", toAuthor.detail());
        assertEquals(100L, captor.getAllValues().get(1).recipientId());
        verify(lifecycleClient, never()).transition(any(), any(), any(), any());
    }

    @Test
    void aFailingNotificationNeverBreaksAnIntermediateEvaluation() {
        ReviewAssignmentEntity assignment = confirmed(201L);
        activeAssignment(assignment);
        when(assignments.countByQuestionIdAndStatus(55L, AssignmentStatus.CONFIRMED)).thenReturn(2L);
        state("IN_REVIEW");
        when(userClient.findById(any())).thenThrow(new IllegalStateException("user-service caído"));

        assertDoesNotThrow(() -> service.submitResult(
                new ReviewResultRequestDTO(55L, 201L, ReviewResult.APPROVED, null)));
    }

    // ------------------------------------------------------------ consultas
    @Test
    void administratorsCanAlwaysViewTheReviewsOfAQuestion() {
        assertDoesNotThrow(() -> service.assertCanView(55L, new AuthenticatedUser(100L, "admin", Set.of("ADMIN"))));
        verify(lifecycleClient, never()).getState(any());
    }

    @Test
    void assignedReviewersCanViewTheReviewsOfTheirQuestion() {
        when(assignments.findByQuestionIdOrderByIdAsc(55L)).thenReturn(List.of(confirmed(201L)));

        assertDoesNotThrow(() -> service.assertCanView(55L, new AuthenticatedUser(201L, "r", Set.of("REVISOR"))));
    }

    @Test
    void theAuthorCanViewTheReviewsToCorrectTheQuestion() {
        when(assignments.findByQuestionIdOrderByIdAsc(55L)).thenReturn(List.of());
        state("REJECTED");

        assertDoesNotThrow(() -> service.assertCanView(55L, new AuthenticatedUser(7L, "autor", Set.of("AUTOR"))));
    }

    @Test
    void strangersCannotViewTheReviewsOfAQuestion() {
        when(assignments.findByQuestionIdOrderByIdAsc(55L)).thenReturn(List.of());
        state("REJECTED");

        assertThrows(ForbiddenException.class,
                () -> service.assertCanView(55L, new AuthenticatedUser(8L, "otro", Set.of("DOCENTE"))));
    }

    @Test
    void viewingTheReviewsOfAnUnknownQuestionFails() {
        when(assignments.findByQuestionIdOrderByIdAsc(55L)).thenReturn(List.of());
        when(lifecycleClient.getState(55L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.assertCanView(55L, new AuthenticatedUser(8L, "otro", Set.of("DOCENTE"))));
    }

    @Test
    void listingsMapAssignmentsAndTheEvaluationHistory() {
        ReviewAssignmentEntity assignment = confirmed(201L);
        when(assignments.findByQuestionIdOrderByIdAsc(55L)).thenReturn(List.of(assignment));
        when(assignments.findByReviewerIdAndStatusOrderByAssignedAtAsc(201L, AssignmentStatus.CONFIRMED))
                .thenReturn(List.of(assignment));
        when(assignments.findById(201L)).thenReturn(Optional.of(assignment));
        when(assignments.findById(999L)).thenReturn(Optional.empty());
        ReviewEntity first = new ReviewEntity(201L, 55L, 201L, ReviewResult.REJECTED, "Falta contexto");
        ReviewEntity second = new ReviewEntity(202L, 55L, 202L, ReviewResult.APPROVED, "ok");
        when(reviews.findByQuestionIdOrderByIdAsc(55L)).thenReturn(List.of(first, second));

        assertEquals(1, service.findByQuestion(55L).size());
        assertEquals(201L, service.findByReviewer(201L).get(0).reviewerId());
        assertEquals(55L, service.findAssignment(201L).questionId());
        assertThrows(NotFoundException.class, () -> service.findAssignment(999L));
        var history = service.findResultsByQuestion(55L);
        assertEquals(2, history.size());
        assertEquals("Falta contexto", history.get(0).comments());
        assertEquals(ReviewResult.APPROVED, history.get(1).result());
    }
}
