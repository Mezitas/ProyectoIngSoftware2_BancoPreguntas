package co.edu.unicauca.review;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
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
import co.edu.unicauca.review.messaging.EventPublisher;
import co.edu.unicauca.review.model.AssignmentStatus;
import co.edu.unicauca.review.model.ReviewAssignmentEntity;
import co.edu.unicauca.review.model.ReviewResult;
import co.edu.unicauca.review.repository.ReviewAssignmentRepository;
import co.edu.unicauca.review.repository.ReviewRepository;
import co.edu.unicauca.review.service.ReviewService;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

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
        when(assignments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void secondReviewerCanBeAssignedWhileQuestionIsInReview() {
        when(userClient.findById(100L)).thenReturn(Optional.of(
                new UserInfoDTO(100L, "admin", "Admin", "admin@example.test", Set.of("ADMIN"))));
        when(userClient.findById(201L)).thenReturn(Optional.of(
                new UserInfoDTO(201L, "reviewer1", "Reviewer 1", "r1@example.test", Set.of("REVISOR"))));
        when(userClient.findById(202L)).thenReturn(Optional.of(
                new UserInfoDTO(202L, "reviewer2", "Reviewer 2", "r2@example.test", Set.of("REVISOR"))));
        when(lifecycleClient.getState(55L))
                .thenReturn(Optional.of(new LifecycleStateDTO(55L, "PENDING_REVIEW", 7L)))
                .thenReturn(Optional.of(new LifecycleStateDTO(55L, "IN_REVIEW", 7L)));
        when(assignments.countByQuestionIdAndStatusIn(any(), any())).thenReturn(0L, 1L);

        service.assign(new AssignRequestDTO(55L, 201L, 100L));
        service.assign(new AssignRequestDTO(55L, 202L, 100L));

        verify(lifecycleClient).transition(55L, "IN_REVIEW", 100L, "Revisor asignado: reviewer1");
        verify(lifecycleClient, never()).transition(55L, "IN_REVIEW", 100L, "Revisor asignado: reviewer2");
        verify(assignments, times(4)).save(any());
        verify(publisher, times(2)).publishReviewerAssigned(any());
    }

    @Test
    void reviewDoesNotFinalizeUntilAllAssignedReviewersHaveResponded() {
        ReviewAssignmentEntity assignment = confirmedAssignment(201L);
        when(assignments.findFirstByQuestionIdAndReviewerIdAndStatus(
                55L, 201L, AssignmentStatus.CONFIRMED)).thenReturn(Optional.of(assignment));
        when(assignments.countByQuestionIdAndStatus(55L, AssignmentStatus.CONFIRMED)).thenReturn(2L);
        when(reviews.existsByAssignmentId(assignment.getId())).thenReturn(false);
        when(reviews.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.submitResult(new ReviewResultRequestDTO(
                55L, 201L, ReviewResult.APPROVED, "Observación del primer revisor"));

        org.junit.jupiter.api.Assertions.assertEquals("IN_REVIEW", result.questionStatus());
        verify(lifecycleClient, never()).transition(any(), any(), any(), any());
    }

    @Test
    void finalReviewTransitionsStateAndNotifiesParticipants() {
        ReviewAssignmentEntity assignment = confirmedAssignment(201L);
        when(assignments.findFirstByQuestionIdAndReviewerIdAndStatus(
                55L, 201L, AssignmentStatus.CONFIRMED)).thenReturn(Optional.of(assignment));
        when(assignments.countByQuestionIdAndStatus(55L, AssignmentStatus.CONFIRMED)).thenReturn(1L);
        when(reviews.existsByAssignmentId(assignment.getId())).thenReturn(false);
        when(reviews.existsByQuestionIdAndResult(55L, ReviewResult.REJECTED)).thenReturn(false);
        when(reviews.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(lifecycleClient.getState(55L))
                .thenReturn(Optional.of(new LifecycleStateDTO(55L, "APPROVED", 7L)));
        when(assignments.findByQuestionIdAndStatusIn(eq(55L), anyCollection()))
                .thenReturn(java.util.List.of(assignment));
        when(userClient.findById(7L)).thenReturn(Optional.of(
                new UserInfoDTO(7L, "author", "Author", "author@example.test", Set.of("AUTOR"))));
        when(userClient.findById(100L)).thenReturn(Optional.of(
                new UserInfoDTO(100L, "admin", "Admin", "admin@example.test", Set.of("ADMIN"))));
        when(userClient.findById(201L)).thenReturn(Optional.of(
                new UserInfoDTO(201L, "reviewer", "Reviewer", "reviewer@example.test", Set.of("REVISOR"))));

        var result = service.submitResult(new ReviewResultRequestDTO(
                55L, 201L, ReviewResult.APPROVED, "Pregunta completa"));

        org.junit.jupiter.api.Assertions.assertEquals("APPROVED", result.questionStatus());
        verify(lifecycleClient).transition(55L, "APPROVED", 201L, "Pregunta completa");
        verify(publisher, times(3)).publishReviewCompleted(any());
    }

    private static ReviewAssignmentEntity confirmedAssignment(Long reviewerId) {
        ReviewAssignmentEntity assignment = new ReviewAssignmentEntity(55L, reviewerId, 100L);
        ReflectionTestUtils.setField(assignment, "id", reviewerId);
        assignment.changeStatus(AssignmentStatus.CONFIRMED, null);
        return assignment;
    }
}
