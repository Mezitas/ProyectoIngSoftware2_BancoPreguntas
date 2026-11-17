package co.edu.unicauca.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.unicauca.review.model.AssignmentStatus;
import co.edu.unicauca.review.model.ReviewAssignmentEntity;
import co.edu.unicauca.review.model.ReviewEntity;
import co.edu.unicauca.review.model.ReviewResult;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ReviewEntitiesTest {

    @Test
    void newAssignmentsStartPending() {
        ReviewAssignmentEntity assignment = new ReviewAssignmentEntity(55L, 201L, 100L);

        assertEquals(55L, assignment.getQuestionId());
        assertEquals(201L, assignment.getReviewerId());
        assertEquals(100L, assignment.getAssignedBy());
        assertEquals(AssignmentStatus.PENDING, assignment.getStatus());
        assertNull(assignment.getFailureReason());
        assertNotNull(assignment.getAssignedAt());
        assertNull(assignment.getId());
    }

    @Test
    void changeStatusRecordsTheReasonAndRefreshesTheTimestamp() {
        ReviewAssignmentEntity assignment = new ReviewAssignmentEntity(55L, 201L, 100L);
        Instant before = assignment.getUpdatedAt();

        assignment.changeStatus(AssignmentStatus.FAILED, "Lifecycle no respondió");

        assertEquals(AssignmentStatus.FAILED, assignment.getStatus());
        assertEquals("Lifecycle no respondió", assignment.getFailureReason());
        assertFalse(assignment.getUpdatedAt().isBefore(before));
    }

    @Test
    void reviewsStartOpenAndCanBeClosedWhenTheRoundEnds() {
        ReviewEntity review = new ReviewEntity(9L, 55L, 201L, ReviewResult.REJECTED, "Falta justificación");

        assertEquals(9L, review.getAssignmentId());
        assertEquals(55L, review.getQuestionId());
        assertEquals(201L, review.getReviewerId());
        assertEquals(ReviewResult.REJECTED, review.getResult());
        assertEquals("Falta justificación", review.getComments());
        assertNotNull(review.getCreatedAt());
        assertFalse(review.isClosed());

        review.close();

        assertTrue(review.isClosed());
    }
}
