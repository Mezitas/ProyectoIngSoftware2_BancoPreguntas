package co.edu.unicauca.review.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unicauca.review.dto.AssignRequestDTO;
import co.edu.unicauca.review.dto.AssignmentDTO;
import co.edu.unicauca.review.dto.ReviewDTO;
import co.edu.unicauca.review.dto.ReviewResultRequestDTO;
import co.edu.unicauca.review.exception.ForbiddenException;
import co.edu.unicauca.review.model.AssignmentStatus;
import co.edu.unicauca.review.model.ReviewResult;
import co.edu.unicauca.review.security.AuthenticatedUser;
import co.edu.unicauca.review.security.CurrentUserProvider;
import co.edu.unicauca.review.service.ReviewService;
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
class ReviewControllerTest {

    @Mock
    private ReviewService service;
    @Mock
    private CurrentUserProvider currentUser;

    private ReviewController controller;

    private final AuthenticatedUser admin = new AuthenticatedUser(100L, "admin", Set.of("ADMIN"));
    private final AuthenticatedUser reviewer = new AuthenticatedUser(201L, "revisor", Set.of("REVISOR"));

    @BeforeEach
    void setUp() {
        controller = new ReviewController(service, currentUser);
    }

    private static AssignmentDTO assignmentDto() {
        return new AssignmentDTO(1L, 55L, 201L, 100L, AssignmentStatus.CONFIRMED, null, Instant.now(), "QUEUED");
    }

    @Test
    void assignTakesTheAdministratorFromTheToken() {
        when(currentUser.current()).thenReturn(admin);
        when(service.assign(any())).thenReturn(assignmentDto());

        var response = controller.assign(new AssignRequestDTO(55L, 201L, null));

        ArgumentCaptor<AssignRequestDTO> captor = ArgumentCaptor.forClass(AssignRequestDTO.class);
        verify(service).assign(captor.capture());
        assertEquals(100L, captor.getValue().assignedBy());
        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void assignRejectsAnAssignedByThatIsNotTheCaller() {
        when(currentUser.current()).thenReturn(admin);

        assertThrows(ForbiddenException.class, () -> controller.assign(new AssignRequestDTO(55L, 201L, 999L)));
        verify(service, never()).assign(any());
    }

    @Test
    void resultTakesTheReviewerFromTheTokenAndRejectsImpersonation() {
        when(currentUser.current()).thenReturn(reviewer);
        when(service.submitResult(any())).thenReturn(
                new ReviewDTO(1L, 1L, 55L, 201L, ReviewResult.APPROVED, "ok", "IN_REVIEW", Instant.now()));

        controller.result(new ReviewResultRequestDTO(55L, null, ReviewResult.APPROVED, "ok"));

        ArgumentCaptor<ReviewResultRequestDTO> captor = ArgumentCaptor.forClass(ReviewResultRequestDTO.class);
        verify(service).submitResult(captor.capture());
        assertEquals(201L, captor.getValue().reviewerId());
        assertThrows(ForbiddenException.class, () -> controller.result(
                new ReviewResultRequestDTO(55L, 202L, ReviewResult.APPROVED, "ok")));
    }

    @Test
    void reviewersOnlyListTheirOwnAssignmentsButAdministratorsListAnyones() {
        when(currentUser.current()).thenReturn(reviewer, reviewer, admin);
        when(service.findByReviewer(201L)).thenReturn(List.of(assignmentDto()));

        assertEquals(1, controller.byReviewer(201L).size());
        assertThrows(ForbiddenException.class, () -> controller.byReviewer(202L));
        assertEquals(1, controller.byReviewer(201L).size());
    }

    @Test
    void questionReviewsAreOnlyReturnedAfterTheAccessCheck() {
        when(currentUser.current()).thenReturn(reviewer);
        when(service.findByQuestion(55L)).thenReturn(List.of(assignmentDto()));
        when(service.findResultsByQuestion(55L)).thenReturn(List.of());

        assertEquals(1, controller.byQuestion(55L).size());
        assertEquals(0, controller.resultsByQuestion(55L).size());
        verify(service, org.mockito.Mockito.times(2)).assertCanView(55L, reviewer);
    }

    @Test
    void anAssignmentIsOnlyReturnedToThoseAllowedToSeeItsQuestion() {
        when(currentUser.current()).thenReturn(reviewer);
        when(service.findAssignment(1L)).thenReturn(assignmentDto());

        assertEquals(55L, controller.assignment(1L).questionId());
        verify(service).assertCanView(55L, reviewer);
    }
}
